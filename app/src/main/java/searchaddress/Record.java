package searchaddress;

public class Record {

    public int ID;
    public String[] val;

    public Record(int primalyID, String value, int valSize) {
        this.ID = primalyID;
        this.val = value.split(",", valSize);
    }
    public Record(int primalyID, String value, int valSize,int[] targetIndex) {
        this.ID = primalyID;
        this.val = value.split(",", valSize);
        sort(targetIndex);
    }

    public int size() {
        return this.val.length;
    }

    public String get(int index) {
        return this.val[index];
    }
    //指定されたindexだけを取り出してレコードの内部リストを再生成する
    public void sort(int[] targetIndex) {
        String[] newVal = new String[targetIndex.length];
        int index = 0;
        for (int i : targetIndex) {
            newVal[index] = this.val[i];
            index++;
        }
        this.val = newVal;
    }
}
