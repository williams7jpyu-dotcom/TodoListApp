package com.todoapp;

import com.todoapp.dao.ToDoDAO;
import com.todoapp.dao.ToDosDAO;
import com.todoapp.logic.ToDoLogic;
import com.todoapp.logic.ToDoListLogic;
import com.todoapp.model.ToDo;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * コンソールアプリケーション メインクラス
 *
 * ユーザーとの対話（コンソール入出力）を担当するクラスです。
 * 手順書ステップ2「Eclipse のコンソールで入出力を行う」に対応しています。
 *
 * 処理の流れ:
 * 1. メニューを表示
 * 2. ユーザーの入力を受け取る
 * 3. 選択された操作に応じてロジッククラスを呼び出す
 * 4. 結果を表示
 * 5. 1に戻る（終了が選択されるまでループ）
 *
 * MVC構造において、このクラスはView（画面表示）とController（入力制御）の役割を兼ねています。
 * フェーズ5でWeb化する際に、このクラスの代わりにサーブレット＋JSPが使われます。
 *
 * フェーズ4での変更点:
 * - DAOの型をToDoDAO（インターフェース）に変更
 * - 編集時に既存のToDo情報を表示するように改善
 */
public class Main {

    // ===== 共有オブジェクト（static: プログラム全体で1つだけ存在） =====

    /**
     * データアクセスオブジェクト（DAOインターフェース型で保持）
     *
     * ToDoDAO型にすることで、ここの1行を変更するだけで
     * ファイル版(ToDoFileDAO)からDB版(ToDosDAO)に切り替えられます。
     * フェーズ4でDB版(ToDosDAO)に切り替え済みです。
     */
    private static final ToDoDAO dao = new ToDosDAO();

    /** 個別ToDo操作用のロジッククラス（追加・更新・削除を担当） */
    private static final ToDoLogic todoLogic = new ToDoLogic(dao);

    /** ToDoリスト取得用のロジッククラス（一覧表示を担当） */
    private static final ToDoListLogic listLogic = new ToDoListLogic(dao);

    /**
     * Scanner: コンソールからのユーザー入力を読み取るためのオブジェクト
     * System.in はキーボードからの入力ストリームを表す
     */
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * メインメソッド: プログラムのエントリーポイント（最初に実行される場所）
     *
     * whileループでメニューを繰り返し表示し、ユーザーの選択に応じた処理を行います。
     * 「0」が入力されるとrunning=falseとなりループを抜けて終了します。
     *
     * @param args コマンドライン引数（このアプリでは使用しない）
     */
    public static void main(String[] args) {
        System.out.println("===== ToDo リスト アプリケーション =====");
        System.out.println();

        // running: プログラムを続行するかどうかのフラグ
        boolean running = true;
        while (running) {
            showMenu();
            String input = scanner.nextLine().trim(); // ユーザーの入力を読み取り、前後の空白を除去

            // switch文: 入力された番号に応じて処理を分岐
            switch (input) {
                case "1":
                    showToDoList();     // 一覧表示
                    break;
                case "2":
                    addToDo();          // 新規追加
                    break;
                case "3":
                    editToDo();         // 編集
                    break;
                case "4":
                    deleteToDo();       // 削除
                    break;
                case "5":
                    toggleCompleted();  // 完了/未完了 切替
                    break;
                case "0":
                    running = false;    // ループを終了するためフラグをfalseに
                    System.out.println("アプリケーションを終了します。");
                    break;
                default:
                    // 想定外の入力の場合
                    System.out.println("無効な選択です。もう一度入力してください。");
            }
            System.out.println(); // メニュー間に空行を入れて見やすくする
        }
        scanner.close(); // Scannerを閉じてリソースを解放する
    }

    /**
     * メニュー表示
     * ユーザーに操作の選択肢を提示する
     */
    private static void showMenu() {
        System.out.println("----- メニュー -----");
        System.out.println("1. ToDo一覧表示");
        System.out.println("2. ToDo新規追加");
        System.out.println("3. ToDo編集");
        System.out.println("4. ToDo削除");
        System.out.println("5. 完了/未完了 切替");
        System.out.println("0. 終了");
        System.out.print("選択 > ");
    }

    /**
     * 一覧表示
     *
     * ToDoリストロジックからリストを取得し、1件ずつ表示します。
     * 設計書の画面概要「ToDoが0件の場合はメッセージ表示」に対応しています。
     * 完了済みのToDoにはチェックマーク(✓)を付けて表示します。
     */
    private static void showToDoList() {
        ArrayList<ToDo> list = listLogic.getToDoList();

        // 設計書: 「ToDoリストがない場合はメッセージを表示」
        if (list.isEmpty()) {
            System.out.println("ToDoリストはありません。");
            return; // 以降の処理をスキップしてメニューに戻る
        }

        System.out.println("----- ToDoリスト -----");
        for (ToDo todo : list) {
            // 完了状態に応じてチェックマークを表示（設計書: チェックボックスに相当）
            String mark = todo.isCompleted() ? "[✓]" : "[ ]";
            System.out.println(mark + " " + todo);
        }
    }

    /**
     * 新規追加
     *
     * アクティビティ図「ToDo新規追加」に対応する処理の流れ:
     * 1. ユーザーにToDo情報の入力を促す
     * 2. ロジッククラスに追加を依頼
     * 3. 結果メッセージを表示
     */
    private static void addToDo() {
        System.out.println("----- ToDo 新規追加 -----");
        // nullを渡すと新規追加モード（番号は自動採番される）
        ToDo todo = inputToDoInfo(null);
        String result = todoLogic.executeAdd(todo);
        System.out.println(result);
    }

    /**
     * 編集
     *
     * アクティビティ図「ToDo編集」に対応する処理の流れ:
     * 1. 現在のリストを表示（どのToDoを編集するか選択するため）
     * 2. 編集対象の番号を入力
     * 3. 既存のToDo情報を表示しつつ、新しい情報を入力
     * 4. ロジッククラスに更新を依頼
     *
     * 編集対象のToDoを取得して既存値を表示するように修正しました。
     * ユーザーがEnterを押すだけで既存値を維持できるようになっています。
     */
    private static void editToDo() {
        showToDoList();
        System.out.print("編集するToDo番号を入力 > ");
        int no = readInt();
        if (no < 0) return; // 無効な入力の場合はメニューに戻る

        // 編集対象のToDoをリストから検索する
        ArrayList<ToDo> list = listLogic.getToDoList();
        ToDo existing = null;
        for (ToDo todo : list) {
            if (todo.getTodoNo() == no) {
                existing = todo;
                break;
            }
        }

        // 対象のToDoが見つからない場合
        if (existing == null) {
            System.out.println("ToDo No." + no + " が見つかりません。");
            return;
        }

        System.out.println("----- ToDo 編集 -----");
        // 既存のToDoを渡して、現在値を表示しながら入力させる
        ToDo todo = inputToDoInfo(existing);
        String result = todoLogic.executeUpdate(todo);
        System.out.println(result);
    }

    /**
     * 削除
     *
     * アクティビティ図「ToDo削除」に対応する処理の流れ:
     * 1. 現在のリストを表示
     * 2. 削除対象の番号を入力
     * 3. 確認ダイアログ（設計書: 「ダイアログで確認する」に対応）
     * 4. OKならロジッククラスに削除を依頼
     */
    private static void deleteToDo() {
        showToDoList();
        System.out.print("削除するToDo番号を入力 > ");
        int no = readInt();
        if (no < 0) return;

        // 設計書の要件: 削除前に確認ダイアログを表示（コンソール版では y/n で代替）
        System.out.print("本当に削除しますか？ (y/n) > ");
        String confirm = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(confirm)) {
            String result = todoLogic.executeDelete(no);
            System.out.println(result);
        } else {
            System.out.println("削除をキャンセルしました。");
        }
    }

    /**
     * 完了/未完了 切替
     *
     * アクティビティ図「ToDoの完了or未完了」に対応する処理の流れ:
     * 1. 現在のリストを表示
     * 2. 切り替え対象の番号を入力
     * 3. 現在の完了状態を反転（完了→未完了 / 未完了→完了）
     */
    private static void toggleCompleted() {
        showToDoList();
        System.out.print("切り替えるToDo番号を入力 > ");
        int no = readInt();
        if (no < 0) return;

        // 現在のリストから対象ToDoを探して、完了状態を反転させる
        ArrayList<ToDo> list = listLogic.getToDoList();
        for (ToDo todo : list) {
            if (todo.getTodoNo() == no) {
                boolean newStatus = !todo.isCompleted(); // 現在の状態を反転
                todoLogic.executeUpdateCompleted(no, newStatus);
                System.out.println("No." + no + " を " + (newStatus ? "完了" : "未完了") + " にしました。");
                return;
            }
        }
        System.out.println("ToDo No." + no + " が見つかりません。");
    }

    /**
     * ToDo情報の入力（追加・編集共通）
     *
     * 新規追加と編集は入力項目が同じ（タスク名、期日、メモ）なので、
     * 処理を1つのメソッドにまとめることで、コードの重複を避けています。
     *
     * フェーズ4での改善点として、
     * existing（既存ToDoオブジェクト）を受け取り、
     * 編集時は現在値を表示し、Enterでスキップすると既存値を維持できるようにしました。
     * 新規追加の場合はexistingがnullなので、通常通り入力を求めます。
     *
     * @param existing 編集対象の既存ToDo（新規追加の場合はnull）
     * @return 入力情報が設定されたToDoオブジェクト
     */
    private static ToDo inputToDoInfo(ToDo existing) {
        ToDo todo = new ToDo();

        // 編集時はToDo番号を引き継ぐ。新規追加時は0（DAOが自動採番する）
        if (existing != null) {
            todo.setTodoNo(existing.getTodoNo());
        }

        // --- タスク名の入力 ---
        if (existing != null) {
            // 編集時: 現在値を表示し、Enterのみで既存値を維持
            System.out.print("タスク名 [現在: " + existing.getTask() + "] > ");
        } else {
            System.out.print("タスク名 > ");
        }
        String taskInput = scanner.nextLine().trim();
        if (taskInput.isEmpty() && existing != null) {
            // Enterのみが押された場合、既存のタスク名をそのまま使う
            todo.setTask(existing.getTask());
        } else {
            todo.setTask(taskInput);
        }

        // --- 期日の入力 ---
        if (existing != null && existing.getDueDate() != null) {
            System.out.print("期日 (yyyy-MM-dd / Enterで現在値維持: " + existing.getDueDate() + ") > ");
        } else {
            System.out.print("期日 (yyyy-MM-dd / Enterで期日なし) > ");
        }
        String dateStr = scanner.nextLine().trim();
        if (dateStr.isEmpty()) {
            // Enterのみの場合、既存値を維持（新規追加ならnull）
            todo.setDueDate(existing != null ? existing.getDueDate() : null);
        } else {
            try {
                // 文字列をLocalDate型に変換（例: "2025-06-15" → LocalDate(2025, 6, 15)）
                todo.setDueDate(LocalDate.parse(dateStr));
            } catch (DateTimeParseException e) {
                // 不正な日付が入力された場合のエラーハンドリング
                System.out.println("日付形式が不正です。期日なしとして登録します。");
                todo.setDueDate(null);
            }
        }

        // --- メモの入力 ---
        if (existing != null && existing.getMemo() != null) {
            System.out.print("メモ [現在: " + existing.getMemo() + "] (Enterで現在値維持) > ");
        } else {
            System.out.print("メモ (Enterでスキップ) > ");
        }
        String memo = scanner.nextLine().trim();
        if (memo.isEmpty()) {
            // Enterのみの場合、既存値を維持（新規追加ならnull）
            todo.setMemo(existing != null ? existing.getMemo() : null);
        } else {
            todo.setMemo(memo);
        }

        // 編集時は完了状態を引き継ぐ（完了/未完了は別メニューで切り替える）
        if (existing != null) {
            todo.setCompleted(existing.isCompleted());
        }

        return todo;
    }

    /**
     * 整数入力ヘルパーメソッド
     *
     * ユーザーがToDo番号を入力する場面が複数あるため（編集、削除、切替）、
     * 数値変換とエラーハンドリングを共通化して重複を避けています。
     *
     * @return 読み取った整数値（変換失敗時は-1を返す）
     */
    private static int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            // 数値でない文字列が入力された場合
            System.out.println("数値を入力してください。");
            return -1; // 呼び出し元で-1を判定してメニューに戻る
        }
    }
}
