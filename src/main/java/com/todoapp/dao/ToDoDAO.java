package com.todoapp.dao;

import com.todoapp.model.ToDo;

import java.util.ArrayList;

/**
 * ToDoデータアクセスオブジェクト（DAO）インターフェース
 *
 * ToDoデータの保存・取得方法を抽象化するための「契約」です。
 * 設計書のクラス図「ToDosDAO」に対応しています。
 *
 * データの保存先を「テキストファイル」から「データベース」に切り替える際に、
 * ロジッククラス（ToDoLogic, ToDoListLogic）のコードを変更しなくて済むようにするため、
 * インターフェースとして定義しています。
 *
 * このインターフェースを実装（implements）するクラスが、実際のデータ操作を行います。
 * - ToDoFileDAO: テキストファイルに保存（フェーズ2で作成済み）
 * - ToDosDAO: MySQLデータベースに保存（フェーズ4で作成）
 *
 * ロジッククラスはこのインターフェース型で参照するため、
 * どちらのDAOを使うかを意識せずにプログラムを書けます（ポリモーフィズム）。
 *
 * 設計書のクラス図との対応として、クラス図「ToDosDAO」のメソッドを
 * そのままインターフェースのメソッドとして定義しています:
 * - ToDo全件取得()     → findAll()
 * - ToDo追加(todo)     → createToDo(todo)
 * - ToDo更新(todo)     → updateToDo(todo)
 * - ToDo状態更新(no, 状態) → updateCompleted(todoNo, completed)
 * - ToDo削除(no)       → deleteToDo(todoNo)
 */
public interface ToDoDAO {

    /**
     * ToDo全件取得
     * データ保存先から全てのToDoデータを取得する
     *
     * @return ToDoのリスト（0件の場合は空のArrayList）
     */
    ArrayList<ToDo> findAll();

    /**
     * ToDo追加（新規作成）
     * 新しいToDoをデータ保存先に追加する
     *
     * @param todo 追加するToDoオブジェクト
     * @return 処理結果のメッセージ
     */
    String createToDo(ToDo todo);

    /**
     * ToDo更新（編集）
     * 既存のToDoの内容を更新する
     *
     * @param todo 更新するToDoオブジェクト（todoNo で対象を特定）
     * @return 処理結果のメッセージ
     */
    String updateToDo(ToDo todo);

    /**
     * ToDo状態更新（完了/未完了の切り替え）
     * 指定されたToDoの完了状態のみを変更する
     *
     * @param todoNo    変更するToDo番号
     * @param completed 新しい完了状態（true=完了, false=未完了）
     * @return 処理結果のメッセージ
     */
    String updateCompleted(int todoNo, boolean completed);

    /**
     * ToDo削除
     * 指定されたToDoをデータ保存先から削除する
     *
     * @param todoNo 削除するToDo番号
     * @return 処理結果のメッセージ
     */
    String deleteToDo(int todoNo);
}
