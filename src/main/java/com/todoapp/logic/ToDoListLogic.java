package com.todoapp.logic;

import com.todoapp.dao.ToDoDAO;
import com.todoapp.model.ToDo;

import java.util.ArrayList;

/**
 * ToDoリストロジッククラス
 *
 * ToDoリスト全体に関する操作（一覧取得）を担当するクラスです。
 * 設計書のクラス図「ToDoリストロジック」に対応しています。
 *
 * 設計書のクラス図では、
 * ToDoLogicは個々のToDoに対する操作（追加/更新/削除）、
 * ToDoListLogicはリスト全体に対する操作（一覧取得）と分けて設計されているため、
 * それに忠実に実装しています。
 * 将来的にリストの検索やフィルタリング機能を追加する場合、
 * このクラスに機能を追加します。
 *
 * フェーズ4での変更点として、
 * DAOの型を具象クラス(ToDoFileDAO)からインターフェース(ToDoDAO)に変更しました。
 */
public class ToDoListLogic {

    /**
     * データアクセスオブジェクト（インターフェース型で保持）
     * ToDoDAO型にすることで、ファイル版/DB版のどちらでも動作する
     */
    private final ToDoDAO dao;

    /**
     * DAOを外部から注入するコンストラクタ
     * ToDoLogicと同じDAOインスタンスを共有するために使用する
     *
     * @param dao 使用するデータアクセスオブジェクト
     */
    public ToDoListLogic(ToDoDAO dao) {
        this.dao = dao;
    }

    /**
     * ToDoリスト取得
     *
     * DAOから全件のToDoデータを取得してリストとして返します。
     * シーケンス図「トップ画面アクセス」の流れに対応します:
     *   トップサーブレット → ToDoリストロジック → ToDosDAO → DB(SELECT全件)
     *
     * @return ToDoのリスト（0件の場合は空のArrayList）
     */
    public ArrayList<ToDo> getToDoList() {
        return dao.findAll();
    }
}
