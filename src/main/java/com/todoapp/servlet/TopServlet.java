package com.todoapp.servlet;

import com.todoapp.dao.ToDoDAO;
import com.todoapp.dao.ToDosDAO;
import com.todoapp.logic.ToDoListLogic;
import com.todoapp.model.ToDo;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * トップ画面サーブレット
 *
 * ToDoリストの一覧を表示するトップ画面を制御するサーブレットです。
 * 設計書のクラス図「トップサーブレット」に対応しています。
 *
 * シーケンス図「トップ画面アクセス」との対応:
 * 1. 利用者がリクエスト → doGet()が呼ばれる
 * 2. ToDoリストロジックにリスト取得を依頼
 * 3. 取得したリストをリクエスト属性にセット
 * 4. top.jspにフォワードして画面を表示
 *
 * @WebServlet("/top") により、/top へのアクセスでこのサーブレットが呼ばれます。
 * web.xmlの<welcome-file>top</welcome-file>により、
 * アプリのルートURL「/」にアクセスした際もこのサーブレットが呼ばれます。
 */
@WebServlet("/top")
public class TopServlet extends HttpServlet {

    /**
     * シリアルバージョンID（設計書のクラス図にも記載あり）
     * Serializableインターフェースの規約で必要な固有識別番号。
     * サーブレットの直列化時に使われる。通常は1Lで問題ない。
     */
    private static final long serialVersionUID = 1L;

    /** データアクセスオブジェクト（DB版） */
    private final ToDoDAO dao = new ToDosDAO();

    /** ToDoリスト取得用ロジッククラス */
    private final ToDoListLogic listLogic = new ToDoListLogic(dao);

    /**
     * GETリクエスト処理（トップ画面表示）
     *
     * 処理の流れ:
     * 1. ToDoリストロジックから全件データを取得
     * 2. 取得したリストをリクエスト属性「todoList」にセット
     * 3. top.jspにフォワードして画面を描画
     *
     * サーブレットからJSPにデータを渡す標準的な方法が「リクエスト属性」です。
     * setAttribute()でセットした値は、JSP側でEL式（${todoList}）で取得できます。
     *
     * @param request  HTTPリクエスト（ブラウザからの要求情報）
     * @param response HTTPレスポンス（ブラウザへの応答情報）
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ToDoリストを取得
        ArrayList<ToDo> todoList = listLogic.getToDoList();

        // リクエスト属性にセット（JSPから ${todoList} でアクセス可能になる）
        request.setAttribute("todoList", todoList);

        // top.jspにフォワード（処理をJSPに委譲して画面を描画する）
        request.getRequestDispatcher("/WEB-INF/jsp/top.jsp")
               .forward(request, response);
    }
}
