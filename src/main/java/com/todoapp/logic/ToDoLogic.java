package com.todoapp.logic;

import com.todoapp.dao.ToDoDAO;
import com.todoapp.model.ToDo;

/**
 * ToDoロジッククラス
 *
 * 個々のToDoに対する操作（追加・更新・状態更新・削除）の
 * ビジネスロジック（業務的な処理の流れ）を担当するクラスです。
 * 設計書のクラス図「ToDoロジック」に対応しています。
 *
 * 画面（View）やデータ保存（DAO）から業務処理（Logic）を分離することで、
 * バリデーション（入力チェック）をここに集約でき、
 * DAOを差し替えても（ファイル→DB）このクラスの変更は最小限で済みます。
 * また、テストがしやすくなる利点もあります。
 *
 * フェーズ4での変更点として、
 * DAOの型を具象クラス(ToDoFileDAO)からインターフェース(ToDoDAO)に変更しました。
 * これにより、ファイル版でもDB版でも同じロジッククラスが使えるようになりました。
 */
public class ToDoLogic {

    /**
     * データアクセスオブジェクト（インターフェース型で保持）
     *
     * ToDoDAO（インターフェース）型で参照することで、
     * ToDoFileDAO（ファイル版）でもToDosDAO（DB版）でも
     * このクラスを変更せずに差し替えが可能になります。
     * これを「ポリモーフィズム（多態性）」と呼びます。
     */
    private final ToDoDAO dao;

    /**
     * DAOを外部から注入するコンストラクタ
     * どのDAO実装を使うかは、このクラスの利用者（Main.javaやサーブレット）が決める。
     * この設計パターンを「依存性の注入（DI）」と呼ぶ。
     *
     * @param dao 使用するデータアクセスオブジェクト（ToDoDAOの実装クラス）
     */
    public ToDoLogic(ToDoDAO dao) {
        this.dao = dao;
    }

    /**
     * 追加実行
     *
     * タスク名が空でないかバリデーション（入力チェック）を行い、
     * チェックOKならDAOに追加を依頼します。
     *
     * @param todo 追加するToDoオブジェクト
     * @return 処理結果のメッセージ（成功 or エラー）
     */
    public String executeAdd(ToDo todo) {
        // バリデーション: タスク名が未入力の場合はエラーを返す
        if (todo.getTask() == null || todo.getTask().trim().isEmpty()) {
            return "タスク名を入力してください。";
        }
        // DAOに追加を依頼し、結果メッセージを返す
        return dao.createToDo(todo);
    }

    /**
     * 更新実行
     *
     * タスク名が空でないかバリデーションを行い、
     * チェックOKならDAOに更新を依頼します。
     *
     * @param todo 更新するToDoオブジェクト（todoNoで対象を特定）
     * @return 処理結果のメッセージ
     */
    public String executeUpdate(ToDo todo) {
        // バリデーション: タスク名が未入力の場合はエラーを返す
        if (todo.getTask() == null || todo.getTask().trim().isEmpty()) {
            return "タスク名を入力してください。";
        }
        return dao.updateToDo(todo);
    }

    /**
     * 状態更新実行（完了/未完了の切り替え）
     *
     * DAOに状態更新を依頼します。
     * boolean値の切り替えのみなので、バリデーションは不要です。
     *
     * @param todoNo    対象のToDo番号
     * @param completed 新しい完了状態
     */
    public void executeUpdateCompleted(int todoNo, boolean completed) {
        dao.updateCompleted(todoNo, completed);
    }

    /**
     * 削除実行
     *
     * DAOに削除を依頼し、結果メッセージを返します。
     * 削除確認ダイアログの表示は画面側（View）の責務なので、
     * ここでは純粋に削除処理だけを行います。
     *
     * @param todoNo 削除するToDo番号
     * @return 処理結果のメッセージ
     */
    public String executeDelete(int todoNo) {
        return dao.deleteToDo(todoNo);
    }
}
