package searchaddress;

import java.io.FileInputStream;
import java.util.Scanner;

public class Table {
    private String csvPath;
    public Record[] records;

    private int recordID = 0;
    private int recordSize;

    public Table(String csvPath) {
        // csvファイルのパスを指定
        this.csvPath = csvPath;
        // 対象のレコード数を行数を数えて取得
        int numOfRecords = 0;
        try (FileInputStream fis = new FileInputStream(this.csvPath);
                Scanner scanner = new Scanner(fis, "Shift-JIS")) {
            //ヘッダー部のスキャン。レコードの要素数をここでカウントする
            if (scanner.hasNext()) {
                this.recordSize = scanner.nextLine().split(",").length;
            }
            while (scanner.hasNext()) {
                scanner.nextLine();
                numOfRecords++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // レコードのリストを作成
        records = new Record[numOfRecords];
    }

    public void insert(String value) {
        Record record = new Record(recordID, value, this.recordSize);
        records[recordID] = record;
        this.recordID ++;
    }

    public void insert(String value,int[] targetIndex) {
        Record record = new Record(recordID, value, this.recordSize,targetIndex);
        records[recordID] = record;
        this.recordID ++;
    }

    public Record get(int recordID) {
        return this.records[recordID];
    }
}
