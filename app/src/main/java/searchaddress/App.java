package searchaddress;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;

import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class App {
    public static void main(String[] args) {
        String csvFilePath = "./csv/zenkoku.csv"; // 検索対象のファイルパス
        int[] targetIndex = { 4, 7, 9, 11, 14, 15, 18, 20 }; // csvの検索に使用する要素の列番号
        int N = 2; // NGramのN
        String invIndexPath = "./invIndex.json"; // 転置インデックスの保存先
        Scanner scanner = new Scanner(System.in, "Shift-JIS"); // 標準入力用のscannerオブジェクト

        // csvがあるか？
        if (new File(csvFilePath).exists()) {
            System.out.println("csvが存在しています");
            // 対象のcsvを読み込んで2次元配列にする
            Table csv = loadCSV(csvFilePath, targetIndex);
            System.out.println("csvの読み込み完了");
            System.out.println("");// 改行

            // 転置インデックスがあるか？
            JSONObject invIndex = null;
            if (new File(invIndexPath).exists()) {
                if (Console.question("既存の転置インデックスが見つかりました。再生成しますか？", scanner)) {
                    System.out.println("転置インデックスを再生成します");
                    invIndex = createInvIndex(csv, N);
                    exportJSON(invIndexPath, invIndex);
                    System.out.println("転置インデックスの再生成が完了しました");
                } else {
                    System.out.println("既存の転置インデックスをロードします");
                    // invIndexオブジェクトに読み込んだファイルを入力
                    invIndex = loadJSON(invIndexPath);
                    System.out.println("ロード完了");
                }
            } else {
                System.out.println("初回起動です。転置インデックスを生成します");
                invIndex = createInvIndex(csv, N);
                exportJSON(invIndexPath, invIndex);
                System.out.println("転置インデックスの生成が完了しました");
            }
            System.out.println("");// 改行

            String charset = Console.selectCharset(scanner);
            scanner = new Scanner(System.in, charset);
            System.out.println("");// 改行

            // 検索のループ開始
            while (true) {
                // 検索キーワードを取得
                String searchWord = Console.inputString("住所検索をします。キーワードを入力 (exitで終了)", scanner);
                if (searchWord.equals("exit")) {
                    break;
                }
                long startTime = System.currentTimeMillis();

                List<String> searchWordArr = NGram.sepString(searchWord, N);
                List<Integer> searchResult = search(searchWordArr, invIndex);
                showResult(searchResult, csv);

                long endTime = System.currentTimeMillis();
                System.out.print("検索結果:" + searchResult.size() + "件");
                System.out.println(" 検索時間:" + (endTime - startTime) + " ms");
                System.out.println("");// 改行
            }
        } else {
            if (!new File("./csv").exists()) {
                System.out.println("csvフォルダがありません。作成します");
                Path csvPath = Paths.get("./csv");
                try {
                    Files.createDirectory(csvPath);
                    System.out.println("csvフォルダを作成しました");
                } catch (Exception e) {
                    System.out.println("csvフォルダの作成に失敗しました");
                }
                System.out.println("");// 改行
            }
            System.out.println("zenkoku.csvファイルがありません。csvフォルダ内に格納してください");
        }
        scanner.close(); // 標準入力のscannerを閉じてmain関数終了
    }

    // csvファイルを読み込み、対象の列を絞り込んで2次元配列化するメソッド
    private static Table loadCSV(String filePath, int[] targetIndex) {
        Table table = new Table(filePath);
        // try with close文でファイル取り込みを行う。charsetNameはShift_JIS。
        try (FileInputStream fis = new FileInputStream(filePath);
                Scanner scanner = new Scanner(fis, "Shift-JIS")) {
            // ヘッダー部分の取り込み
            scanner.nextLine();
            // 読み取る行が無くなるまで読み取り続ける
            while (scanner.hasNext()) {
                // csvから1行読み取り、""を削除（CSVには文字列をダブルクォーテーションで区切るものと区切らないものがあるため）
                String line = scanner.nextLine().replace("\"", "");
                // レコードを取り込み。targetIndexは取り込む対象の列の指定
                table.insert(line, targetIndex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return table;
    }

    // csvの2次元配列を受け取り、NGramによる転置インデックスのJSONデータを作成するメソッド
    private static JSONObject createInvIndex(Table csv, int N) {
        // 転置インデックスを格納する変数を用意。キー:単語 , 値:recordIDのリスト で与える
        HashMap<String, List<Integer>> invIndex = new HashMap<>();

        for (Record record : csv.records) { // 2次元配列を展開し、1番要素から順にNGramを実行(0番は郵便番号=検索対象外のためスキップ)
            List<String> searchTarget = new ArrayList<String>();
            for (int i = 1; i < record.size(); i++) {
                // 空白要素以外を取り込み、スペースを削除
                if (!record.get(i).equals("")) {
                    searchTarget.add(record.get(i).replace("　", ""));
                }
            }
            // searchTargetの各単語についてNGram(ここでは N = 2)を実行し、出来た結果をwordsに格納する
            List<String> words = NGram.makeNGram(searchTarget, N);
            // 得られたwordsについて、invIndexの該当するキーにrecordIDを登録する
            for (String word : words) {
                // 対象wordのキーがなかった時、新規にmapにキーとリストを追加
                invIndex.putIfAbsent(word, new ArrayList<Integer>());
                // 対象wordのvalueにrecordIDを追加する
                invIndex.get(word).add(record.ID);
            }
        }
        return new JSONObject(invIndex);
    }

    // 入力されたJSONデータを指定のパスにファイルとして出力するメソッド
    private static void exportJSON(String filePath, JSONObject json) {
        try (FileOutputStream fos = new FileOutputStream(filePath);
            OutputStreamWriter writer = new OutputStreamWriter(fos,"UTF-8")) {
            writer.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // JSONファイルを読み込んで、JSONObjectにして返すメソッド
    private static JSONObject loadJSON(String filePath) {
        JSONObject json = new JSONObject();
        try (FileInputStream fis = new FileInputStream(filePath);
                Scanner scanner = new Scanner(fis,"UTF-8")) {
            if (scanner.hasNext()) {
                json = new JSONObject(scanner.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }
    
    // 受け取った検索ワードリストで転置インデックスへの検索を行い、ヒットしたレコードのIDをリストで返すメソッド
    private static List<Integer> search(List<String> searchWordArr, JSONObject invIndex) {
        List<Integer> searchResult = new ArrayList<>();
        for (String searchWord : searchWordArr) {
            // invIndexから検索ワードで検索をし、結果をJSONArrayで保存
            JSONArray hitIDs = new JSONArray();
            try {
                hitIDs = new JSONArray(invIndex.get(searchWord).toString());
            } catch (JSONException e) {
                ;
            }
            // hitID内のrecordIDらをresultに一つずつ格納
            for (int i = 0; i < hitIDs.length(); i++) {
                int recordID = hitIDs.getInt(i);
                searchResult.add(recordID);
            }
        }
        // 重複を削除してreturn
        return searchResult.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    // 転置インデックスからヒットしたRecordIDを使って、住所情報を標準出力するメソッド
    private static void showResult(List<Integer> searchResult, Table csv) {
        for (Integer RecordID : searchResult) {
            Record record = csv.get(RecordID);
            for (int i = 0; i < record.size(); i++) {
                if (i == 0) { // 郵便番号
                    System.out.print("〒" + record.get(i));
                } else if (i == 1) { // 都道府県
                    System.out.print("　" + record.get(i));
                } else if (i == 2) { // 市区町村
                    System.out.print(record.get(i));
                } else if (!record.get(i).equals("")) { // 以降
                    System.out.print("　" + record.get(i));
                }
            }
            // 改行
            System.out.print("\n");
        }
    }
}
