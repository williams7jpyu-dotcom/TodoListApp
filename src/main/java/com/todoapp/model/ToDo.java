package com.todoapp.model;

import java.time.LocalDate;

/**
 * ToDoモデルクラス
 *
 * ToDoリストの1件分のデータを保持するクラスです。
 * 設計書のクラス図「ToDoモデル」に対応しています。
 *
 * アプリケーション内でToDoデータを扱うとき、
 * 「番号」「タスク名」「期日」「完了状態」「メモ」をまとめて1つのオブジェクトとして
 * 管理するためにこのクラスを使います。
 * こうすることで、データの受け渡しが簡潔になります。
 *
 * フィールド:
 * - todoNo: ToDo番号（各ToDoを一意に識別するための番号）
 * - task: タスク名（やること の内容を表す文字列）
 * - dueDate: 期日（いつまでにやるか。nullの場合は期日なし）
 * - completed: 完了状態（true=完了, false=未完了）
 * - memo: メモ（補足情報。nullの場合はメモなし）
 */
public class ToDo {

    // ===== フィールド（ToDoの情報を保持する変数） =====

    /** ToDo番号: データベースやファイルで各ToDoを区別するために使う一意の番号 */
    private int todoNo;

    /** タスク名: ユーザーが入力する「やること」の内容 */
    private String task;

    /**
     * 期日: ToDoの締め切り日
     * LocalDate型を使うことで、日付の比較や書式変換が簡単にできる
     * nullの場合は「期日なし」を意味する
     */
    private LocalDate dueDate;

    /** 完了状態: true=完了（やり終わった）, false=未完了（まだやっていない） */
    private boolean completed;

    /** メモ: タスクに関する補足情報（任意入力なのでnullも許容） */
    private String memo;

    // ===== コンストラクタ =====

    /**
     * デフォルトコンストラクタ（引数なし）
     * フレームワークやJSPからのデータバインドで使われることがあるため、
     * 引数なしのコンストラクタを用意しておく
     */
    public ToDo() {
    }

    /**
     * 全フィールド指定コンストラクタ
     * ファイルやデータベースから読み込んだデータをまとめてToDoオブジェクトに
     * 変換するときに便利なコンストラクタ
     *
     * @param todoNo    ToDo番号
     * @param task      タスク名
     * @param dueDate   期日（nullで期日なし）
     * @param completed 完了状態
     * @param memo      メモ（nullでメモなし）
     */
    public ToDo(int todoNo, String task, LocalDate dueDate, boolean completed, String memo) {
        this.todoNo = todoNo;
        this.task = task;
        this.dueDate = dueDate;
        this.completed = completed;
        this.memo = memo;
    }

    // ===== ゲッター / セッター =====
    // JavaBeans規約に従い、各フィールドにget/setメソッドを用意する。
    // これにより、JSPやフレームワークからフィールドにアクセスできるようになる。

    /** ToDo番号を取得する */
    public int getTodoNo() {
        return todoNo;
    }

    /** ToDo番号を設定する（新規追加時にDAOが自動採番して設定する） */
    public void setTodoNo(int todoNo) {
        this.todoNo = todoNo;
    }

    /** タスク名を取得する */
    public String getTask() {
        return task;
    }

    /** タスク名を設定する（ユーザーが入力した内容を格納する） */
    public void setTask(String task) {
        this.task = task;
    }

    /** 期日を取得する（nullの場合は期日なし） */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /** 期日を設定する（nullを設定すると期日なしになる） */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * 完了状態を取得する
     * boolean型のゲッターは慣習的に「is〜」という名前にする
     */
    public boolean isCompleted() {
        return completed;
    }

    /** 完了状態を設定する（チェックボックスの切り替え時に使う） */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    /** メモを取得する */
    public String getMemo() {
        return memo;
    }

    /** メモを設定する */
    public void setMemo(String memo) {
        this.memo = memo;
    }

    /**
     * ToDoの情報を人が読める文字列として返すメソッド
     * デバッグやコンソール表示で使う。
     * System.out.println(todo) と書くだけでこの形式で出力される。
     */
    @Override
    public String toString() {
        // 完了状態を日本語で表示
        String status = completed ? "完了" : "未完了";
        // 期日がnullの場合は「期日なし」と表示
        String dateStr = (dueDate != null) ? dueDate.toString() : "期日なし";
        // String.format で見やすい形式にフォーマットして返す
        return String.format("[%d] %s | 期日: %s | 状態: %s | メモ: %s",
                todoNo, task, dateStr, status, (memo != null ? memo : ""));
    }
}
