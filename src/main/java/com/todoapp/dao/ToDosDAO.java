package com.todoapp.dao;

import com.todoapp.model.ToDo;

import java.sql.*;
import java.util.ArrayList;

/**
 * MySQL データベースによるToDoデータのストレージ（データアクセスオブジェクト）
 *
 * ToDoデータをMySQLデータベースに保存・読込するクラスです。
 * ToDoDAOインターフェースを実装（implements）しており、
 * 設計書のクラス図「ToDosDAO」に直接対応しています。
 *
 * ToDoFileDAOとの違いとして、
 * ToDoFileDAOはテキストファイル(todos.txt)に保存（フェーズ2）、
 * ToDosDAO(このクラス)はMySQLデータベースに保存（フェーズ4）します。
 * 両方とも同じToDoDAOインターフェースを実装しているため、
 * ロジッククラスを変更せずに切り替えが可能です。
 *
 * DB接続については、各メソッドで毎回Connectionを取得・クローズする方式を採用しています。
 * try-with-resourcesを使うことで、処理完了後に自動的にDB接続が閉じられるため、
 * 接続の閉じ忘れ（コネクションリーク）を防ぐことができます。
 *
 * 使用するSQL文:
 * - SELECT: ToDo全件取得
 * - INSERT: ToDo追加
 * - UPDATE: ToDo更新、ToDo状態更新
 * - DELETE: ToDo削除
 */
public class ToDosDAO implements ToDoDAO {

    // ===== データベース接続情報 =====
    // 定数（static final）として定義することで、接続先を一箇所で管理できる

    /**
     * JDBC接続URL
     * jdbc:mysql://ホスト名:ポート番号/データベース名
     * useSSL=false: ローカル開発ではSSL接続を無効化
     * allowPublicKeyRetrieval=true: パスワード認証の取得を許可
     * serverTimezone=Asia/Tokyo: タイムゾーンを日本に設定
     */
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/todo_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Tokyo";

    /** データベースのユーザー名（フェーズ0でrootパスワードなしで初期化した） */
    private static final String DB_USER = "root";

    /** データベースのパスワード（初期化時に --initialize-insecure を使ったため空） */
    private static final String DB_PASSWORD = "";

    /*
     * JDBCドライバは通常SPI（ServiceProviderInterface）で自動検出されますが、
     * Tomcatではクラスローダーが分離されているため、
     * WEB-INF/libに置いたJARのドライバが自動検出されないことがあります。
     * Class.forName()でドライバクラスを明示的にロードすることで、
     * DriverManagerにドライバを登録し、確実にDB接続できるようにしています。
     * staticイニシャライザなので、このクラスが初めて使われる時に1回だけ実行されます。
     */
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBCドライバが見つかりません。", e);
        }
    }

    /**
     * データベース接続を取得する（内部ヘルパーメソッド）
     *
     * DB接続の取得処理を一箇所にまとめることで、
     * 接続先の変更が必要になった場合にここだけ修正すれば済むようにしています。
     *
     * @return データベースへのConnection（接続）オブジェクト
     * @throws SQLException 接続に失敗した場合
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * ToDo全件取得
     *
     * todosテーブルから全レコードをSELECTし、ToDoオブジェクトのリストとして返します。
     * シーケンス図「トップ画面アクセス」のステップ3-5に対応します:
     *   ToDosDAO → DB(SELECT全件) → 結果を返す
     *
     * SQL文:
     * SELECT todo_no, task, due_date, completed, memo FROM todos ORDER BY todo_no
     * ORDER BYでToDo番号順に並べることで、表示順を安定させています。
     *
     * @return ToDoのリスト（0件の場合は空のArrayList）
     */
    @Override
    public ArrayList<ToDo> findAll() {
        ArrayList<ToDo> list = new ArrayList<>();

        // SQL文を定数として定義（可読性のため）
        String sql = "SELECT todo_no, task, due_date, completed, memo FROM todos ORDER BY todo_no";

        // try-with-resources: Connection → PreparedStatement → ResultSet を順に開き、
        // 処理完了後に自動的にすべて閉じる（逆順で閉じられる）
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // ResultSetから1行ずつデータを読み取り、ToDoオブジェクトに変換
            while (rs.next()) {
                ToDo todo = new ToDo();
                todo.setTodoNo(rs.getInt("todo_no"));
                todo.setTask(rs.getString("task"));

                // DATE型のカラムをLocalDateに変換
                // rs.getDate()はnullを返すことがあるため、nullチェックが必要
                Date sqlDate = rs.getDate("due_date");
                todo.setDueDate(sqlDate != null ? sqlDate.toLocalDate() : null);

                // MySQLのBOOLEAN(TINYINT(1))をboolean型として取得
                todo.setCompleted(rs.getBoolean("completed"));
                todo.setMemo(rs.getString("memo"));

                list.add(todo);
            }
        } catch (SQLException e) {
            // DB接続やSQL実行に失敗した場合はエラーメッセージを表示
            System.err.println("ToDo全件取得エラー: " + e.getMessage());
        }

        return list;
    }

    /**
     * ToDo追加（新規作成）
     *
     * todosテーブルにINSERT文で新しいレコードを追加します。
     * シーケンス図「ToDo新規追加」のステップ6-8に対応します:
     *   ToDosDAO → DB(INSERT) → 結果を返す
     *
     * SQL文:
     * INSERT INTO todos (task, due_date, completed, memo) VALUES (?, ?, ?, ?)
     * todo_noはAUTO_INCREMENTなので指定不要（DBが自動採番します）。
     * ?はプレースホルダで、PreparedStatementで値をセットすることでSQLインジェクションを防ぎます。
     *
     * @param todo 追加するToDoオブジェクト
     * @return 処理結果のメッセージ
     */
    @Override
    public String createToDo(ToDo todo) {
        String sql = "INSERT INTO todos (task, due_date, completed, memo) VALUES (?, ?, ?, ?)";

        // Statement.RETURN_GENERATED_KEYS: INSERTで自動採番されたIDを取得するためのオプション
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // プレースホルダ(?)に値をセット（1から始まるインデックス）
            pstmt.setString(1, todo.getTask());

            // LocalDateをjava.sql.Dateに変換してセット（nullの場合はnullをセット）
            if (todo.getDueDate() != null) {
                pstmt.setDate(2, Date.valueOf(todo.getDueDate()));
            } else {
                pstmt.setNull(2, Types.DATE);
            }

            pstmt.setBoolean(3, todo.isCompleted());

            // メモがnullの場合はnullをセット
            if (todo.getMemo() != null) {
                pstmt.setString(4, todo.getMemo());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            // SQL実行（INSERT/UPDATE/DELETEにはexecuteUpdate()を使う）
            pstmt.executeUpdate();

            // 自動採番されたtodo_noを取得してToDoオブジェクトに設定
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    todo.setTodoNo(generatedKeys.getInt(1));
                }
            }

            return "追加しました。(No." + todo.getTodoNo() + ")";
        } catch (SQLException e) {
            return "追加に失敗しました: " + e.getMessage();
        }
    }

    /**
     * ToDo更新（編集）
     *
     * todosテーブルの指定されたtodo_noのレコードをUPDATEします。
     * シーケンス図「ToDo編集」のステップ8-10に対応します:
     *   ToDosDAO → DB(UPDATE) → 結果を返す
     *
     * SQL文:
     * UPDATE todos SET task=?, due_date=?, completed=?, memo=? WHERE todo_no=?
     * WHERE句でtodo_noを指定して、特定の1レコードだけを更新します。
     *
     * @param todo 更新するToDoオブジェクト（todoNoで対象を特定）
     * @return 処理結果のメッセージ
     */
    @Override
    public String updateToDo(ToDo todo) {
        String sql = "UPDATE todos SET task = ?, due_date = ?, completed = ?, memo = ? WHERE todo_no = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, todo.getTask());

            if (todo.getDueDate() != null) {
                pstmt.setDate(2, Date.valueOf(todo.getDueDate()));
            } else {
                pstmt.setNull(2, Types.DATE);
            }

            pstmt.setBoolean(3, todo.isCompleted());

            if (todo.getMemo() != null) {
                pstmt.setString(4, todo.getMemo());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            // WHERE条件: todo_noが一致するレコードを更新
            pstmt.setInt(5, todo.getTodoNo());

            // executeUpdate()は影響を受けた行数を返す
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                // 0行 = 該当するtodo_noのレコードが存在しなかった
                return "ToDo No." + todo.getTodoNo() + " が見つかりません。";
            }
            return "更新しました。(No." + todo.getTodoNo() + ")";
        } catch (SQLException e) {
            return "更新に失敗しました: " + e.getMessage();
        }
    }

    /**
     * ToDo状態更新（完了/未完了の切り替え）
     *
     * todosテーブルの指定されたtodo_noのcompletedカラムのみをUPDATEします。
     * シーケンス図「ToDo完了or未完了」のステップ3-4に対応します。
     *
     * 状態更新はcompletedフィールドのみの変更なので、
     * 全フィールドを更新するupdateToDoとは別メソッドにして効率化しています。
     * 設計書のクラス図でも別メソッドとして定義されています。
     *
     * @param todoNo    変更するToDo番号
     * @param completed 新しい完了状態
     * @return 処理結果のメッセージ
     */
    @Override
    public String updateCompleted(int todoNo, boolean completed) {
        String sql = "UPDATE todos SET completed = ? WHERE todo_no = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, completed);
            pstmt.setInt(2, todoNo);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                return "ToDo No." + todoNo + " が見つかりません。";
            }
            return "状態を更新しました。(No." + todoNo + ")";
        } catch (SQLException e) {
            return "状態更新に失敗しました: " + e.getMessage();
        }
    }

    /**
     * ToDo削除
     *
     * todosテーブルから指定されたtodo_noのレコードをDELETEします。
     * シーケンス図「ToDo削除」のステップ5-7に対応します:
     *   ToDosDAO → DB(DELETE) → 結果を返す
     *
     * @param todoNo 削除するToDo番号
     * @return 処理結果のメッセージ
     */
    @Override
    public String deleteToDo(int todoNo) {
        String sql = "DELETE FROM todos WHERE todo_no = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, todoNo);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                return "ToDo No." + todoNo + " が見つかりません。";
            }
            return "削除しました。(No." + todoNo + ")";
        } catch (SQLException e) {
            return "削除に失敗しました: " + e.getMessage();
        }
    }
}
