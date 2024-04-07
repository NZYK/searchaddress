package searchaddress;

import java.util.Scanner;

//コンソールアプリケーションを作りやすくするためのクラス。今回は使用していないが表示内容の書き換え機能なども持つ
public class Console {
    private String nowString;

    public Console(String initialString) {
        this.nowString = initialString;
    }

    public void write() {
        System.out.print("\r" + this.nowString);
    }

    // writeのオーバーロード
    public void write(String string) {
        this.nowString = this.fillSpace(string);
        this.write();
    }

    // 直前に表示されていた文字と重ならないようにスペースで埋める
    private String fillSpace(String string) {
        int space = this.nowString.length() - string.length();
        if (space > 0) {
            string = string + "　".repeat(space);
        }
        return string;
    }

    //yes no の質問を投げかけることができる
    public static boolean question(String question,Scanner scanner) {
        boolean result;
        while (true) { // 入力がyかnになるまで繰り返す
            System.out.print(question+" (y/n) > ");
            String answer = scanner.nextLine();
            if (answer.equals("y")) {
                result = true;
                break;
            } else if (answer.equals("n")) {
                result = false;
                break;
            } 
        }
        return result;
    }

    //自由入力で一行の標準入力を受け付けられる
    public static String inputString(String message,Scanner scanner) {
        String inputString;
        while (true) { // 文字列が入力されるまで繰り返す
            System.out.print(message+" > ");
            String answer = scanner.nextLine();
            if (!answer.equals("")) {
                inputString = answer;
                break;
            }
        }
        return inputString;
    }

    public static String selectCharset(Scanner scanner) {
        String inpStr = "";
        String charset = "";
        while (true) {
            System.out.println("標準入力に使用する文字コードを選択してください\n基本的に、windowsならShift-JIS、linux,macならUTF-8です");
            inpStr = inputString("(s) Shift-JIS / (u) UTF-8", scanner);
            if (inpStr.equals("s")){
                charset = "Shift-JIS";
                break;
            }else if (inpStr.equals("u")){
                charset = "UTF-8";
                break;
            }
        }
        return charset;
    }
}
