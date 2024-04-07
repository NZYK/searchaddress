package searchaddress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NGram {

    //入力された文字列のNGram化をリストで返す
    public static List<String> sepString(String inputString, int N) {
        // 入力文字列の大きさがNより大きい時、切り出し操作を実行
        if (N < inputString.length()) {
            //return用に切り出し後の文字列リストを用意
            List<String> separatedStrings = new ArrayList<String>();

            for (int i = 0; i + N <= inputString.length(); i++) {
                // N文字で切り出し
                String word = inputString.substring(i, i + N);
                separatedStrings.add(word);
            }
            return separatedStrings;
        } else {
            // N文字以下の場合はそのまま配列化して返す
            return Arrays.asList(inputString);
        }
    }

    // 文字列集List<String>の入力に対してNGramのリストを作成するメソッド
    public static List<String> makeNGram(List<String> recordForSearch, int N) {
        //return用のリストを用意
        List<String> NGram = new ArrayList<String>();

        for (String colString : recordForSearch) {
            //sepStringメソッドを使って全てのカラムについて分割操作を行う
            List<String> separatdStrings = sepString(colString, N);
            //分割後の各単語はNGramリストに格納する
            for (String word : separatdStrings) {
                NGram.add(word);
            }
        }
        //重複を削除して返す
        return NGram.stream()
                .distinct()
                .collect(Collectors.toList());
    }
}
