package com.todoapp.servlet;

import com.todoapp.dao.ToDoDAO;
import com.todoapp.dao.ToDosDAO;
import com.todoapp.logic.ToDoListLogic;
import com.todoapp.logic.ToDoLogic;
import com.todoapp.model.ToDo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * メインサーブレット（CRUD操作を処理）
 *
 * ToDo詳細画面の表示、入力フォーム画面の表示、
 * 追加・更新・削除・状態更新の処理を担当するサーブレットです。
 * 設計書のクラス図「メインサーブレット」に対応しています。
 *
 * @WebServlet("/main") により、/main へのアクセスでこのサーブレットが呼ばれます。
 *
 * 処理の振り分け:
 * GETリクエストでは画面表示（詳細画面 or 入力フォーム画面）を行います:
 *   - action=detail → ToDo詳細画面
 *   - action=form   → ToDo入力フォーム画面（新規追加 / 編集）
 * POSTリクエストではデータ操作（追加 / 更新 / 削除 / 状態更新）を行います:
 *   - action=add       → 新規追加
 *   - action=update    → 更新
 *   - action=delete    → 削除
 *   - action=toggle    → 完了/未完了切替
 */
@WebServlet("/main")
public class MainServlet extends HttpServlet {

    /** シリアルバージョンID（設計書のクラス図に記載） */
    private static final long serialVersionUID = 1L;

    /** データアクセスオブジェクト（DB版） */
    private final ToDoDAO dao = new ToDosDAO();

    /** 個別ToDo操作用ロジッククラス */
    private final ToDoLogic todoLogic = new ToDoLogic(dao);

    /** ToDoリスト取得用ロジッククラス */
    private final ToDoListLogic listLogic = new ToDoListLogic(dao);

    /**
     * GETリクエスト処理（画面表示）
     *
     * 処理の流れ:
     * actionパラメータに応じて表示する画面を切り替える。
     *
     * ■ action=detail（ToDo詳細画面）
     * シーケンス図「ToDo編集」ステップ1-2、「ToDo削除」ステップ1-2:
     *   利用者がToDoリンクを押す → ToDo詳細画面を表示する
     *
     * ■ action=form（ToDo入力フォーム画面）
     * シーケンス図「ToDo新規追加」ステップ1-2:
     *   新規追加ボタンを押す → ToDo入力フォーム画面を表示する
     * シーケンス図「ToDo編集」ステップ3-4:
     *   編集ボタンを押す → ToDo入力フォーム画面を表示する
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // リクエストの文字エンコーディングをUTF-8に設定（日本語文字化け防止）
        request.setCharacterEncoding("UTF-8");

        // actionパラメータで処理を分岐
        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "detail":
                // === ToDo詳細画面の表示 ===
                showDetail(request, response);
                break;

            case "form":
                // === ToDo入力フォーム画面の表示 ===
                showForm(request, response);
                break;

            default:
                // actionが不明な場合はトップ画面にリダイレクト
                response.sendRedirect(request.getContextPath() + "/top");
                break;
        }
    }

    /**
     * POSTリクエスト処理（データ操作）
     *
     * actionパラメータに応じてCRUD操作を実行し、トップ画面にリダイレクトします。
     *
     * POST処理後にリダイレクトすることで、
     * ブラウザの「更新」ボタンを押した際にPOSTが再送信されるのを防いでいます。
     * この設計パターンを「PRG（Post-Redirect-Get）パターン」と呼びます。
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // リクエストの文字エンコーディングをUTF-8に設定（日本語文字化け防止）
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action) {
            case "add":
                // === ToDo新規追加 ===
                // シーケンス図「ToDo新規追加」ステップ4-9に対応
                doAdd(request);
                break;

            case "update":
                // === ToDo更新（編集） ===
                // シーケンス図「ToDo編集」ステップ6-11に対応
                doUpdate(request);
                break;

            case "delete":
                // === ToDo削除 ===
                // シーケンス図「ToDo削除」ステップ4-8に対応
                doDelete(request);
                break;

            case "toggle":
                // === 完了/未完了 切替 ===
                // シーケンス図「ToDo完了or未完了」に対応
                doToggle(request);
                break;

            default:
                break;
        }

        // PRGパターン: 処理後はトップ画面にリダイレクト
        response.sendRedirect(request.getContextPath() + "/top");
    }

    // ===== 画面表示メソッド（doGetから呼ばれる） =====

    /**
     * ToDo詳細画面の表示
     *
     * 指定されたtodoNoのToDoを取得し、detail.jspに表示します。
     * 画面遷移図: トップ画面 → (ToDoリンク) → ToDo詳細画面
     */
    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // todoNoパラメータを取得して対象のToDoを検索
        int todoNo = parseIntParam(request, "todoNo", -1);

        // 全件からtodoNoが一致するものを探す
        ArrayList<ToDo> list = listLogic.getToDoList();
        ToDo todo = null;
        for (ToDo t : list) {
            if (t.getTodoNo() == todoNo) {
                todo = t;
                break;
            }
        }

        if (todo == null) {
            // 該当するToDoが見つからない場合はトップ画面にリダイレクト
            response.sendRedirect(request.getContextPath() + "/top");
            return;
        }

        // リクエスト属性にToDoオブジェクトをセットしてJSPへ
        request.setAttribute("todo", todo);
        request.getRequestDispatcher("/WEB-INF/jsp/detail.jsp")
               .forward(request, response);
    }

    /**
     * ToDo入力フォーム画面の表示
     *
     * 新規追加の場合は空のフォームを表示し、
     * 編集の場合は既存のToDo情報をフォームに表示します。
     *
     * 画面遷移図:
     *   トップ画面 → (新規追加) → ToDo入力フォーム画面
     *   ToDo詳細画面 → (編集) → ToDo入力フォーム画面
     *
     * todoNoパラメータがあれば編集モード、なければ新規追加モードと判定します。
     */
    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int todoNo = parseIntParam(request, "todoNo", 0);

        if (todoNo > 0) {
            // 編集モード: 既存ToDoをフォームに表示
            ArrayList<ToDo> list = listLogic.getToDoList();
            for (ToDo t : list) {
                if (t.getTodoNo() == todoNo) {
                    request.setAttribute("todo", t);
                    break;
                }
            }
        }
        // 新規追加モードではtodo属性はnull → JSP側で判定する

        request.getRequestDispatcher("/WEB-INF/jsp/form.jsp")
               .forward(request, response);
    }

    // ===== データ操作メソッド（doPostから呼ばれる） =====

    /**
     * ToDo新規追加
     *
     * シーケンス図「ToDo新規追加」ステップ4-9:
     *   ToDoインスタンスを生成 → ToDoを渡す → executeAdd() → createToDo() → INSERT → 結果を返す
     */
    private void doAdd(HttpServletRequest request) {
        ToDo todo = buildToDoFromRequest(request);
        todoLogic.executeAdd(todo);
    }

    /**
     * ToDo更新（編集）
     *
     * シーケンス図「ToDo編集」ステップ6-11:
     *   ToDoインスタンスを生成 → executeUpdate() → updateToDo() → UPDATE → 結果を返す
     */
    private void doUpdate(HttpServletRequest request) {
        ToDo todo = buildToDoFromRequest(request);
        todo.setTodoNo(parseIntParam(request, "todoNo", 0));
        todoLogic.executeUpdate(todo);
    }

    /**
     * ToDo削除
     *
     * シーケンス図「ToDo削除」ステップ4-8:
     *   executeDelete() → deleteToDo() → DELETE → 結果を返す
     */
    private void doDelete(HttpServletRequest request) {
        int todoNo = parseIntParam(request, "todoNo", -1);
        if (todoNo > 0) {
            todoLogic.executeDelete(todoNo);
        }
    }

    /**
     * 完了/未完了 切替
     *
     * シーケンス図「ToDo完了or未完了」:
     *   チェックボックスの状態を反転して更新
     */
    private void doToggle(HttpServletRequest request) {
        int todoNo = parseIntParam(request, "todoNo", -1);
        // completedパラメータはチェックボックスがチェックされている場合に送信される
        // チェックが入っている = "true" or "on" → 完了にする
        // チェックが入っていない = パラメータが送信されない → 未完了にする
        String completedParam = request.getParameter("completed");
        boolean completed = "true".equals(completedParam);

        if (todoNo > 0) {
            todoLogic.executeUpdateCompleted(todoNo, completed);
        }
    }

    // ===== ヘルパーメソッド =====

    /**
     * リクエストパラメータからToDoオブジェクトを生成
     *
     * 新規追加と編集はフォームの入力項目が同じなので、
     * リクエストパラメータからToDoへの変換処理を共通化しています。
     *
     * @param request HTTPリクエスト
     * @return 入力値が設定されたToDoオブジェクト
     */
    private ToDo buildToDoFromRequest(HttpServletRequest request) {
        ToDo todo = new ToDo();

        // タスク名を取得
        todo.setTask(request.getParameter("task"));

        // 期日を取得してLocalDateに変換
        String dueDateStr = request.getParameter("dueDate");
        if (dueDateStr != null && !dueDateStr.trim().isEmpty()) {
            try {
                todo.setDueDate(LocalDate.parse(dueDateStr.trim()));
            } catch (DateTimeParseException e) {
                // 不正な日付形式の場合はnull（期日なし）
                todo.setDueDate(null);
            }
        }

        // メモを取得（空文字列はnullに変換）
        String memo = request.getParameter("memo");
        todo.setMemo(memo != null && !memo.trim().isEmpty() ? memo.trim() : null);

        // 完了状態を取得（編集時に既存の完了状態を引き継ぐ）
        String completedStr = request.getParameter("completed");
        todo.setCompleted("true".equals(completedStr));

        return todo;
    }

    /**
     * リクエストパラメータから整数値を安全に取得するヘルパーメソッド
     *
     * @param request      HTTPリクエスト
     * @param paramName    パラメータ名
     * @param defaultValue 変換失敗時のデフォルト値
     * @return パラメータの整数値（変換失敗時はdefaultValue）
     */
    private int parseIntParam(HttpServletRequest request, String paramName, int defaultValue) {
        String value = request.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
