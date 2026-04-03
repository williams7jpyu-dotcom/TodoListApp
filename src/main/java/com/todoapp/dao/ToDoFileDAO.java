package com.todoapp.dao;

import com.todoapp.model.ToDo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * テキストファイルによるToDoデータのストレージ（データアクセスオブジェクト）
 *
 * ToDoデータをテキストファイル(todos.txt)に保存・読込するクラスです。
 * ToDoDAOインターフェースを実装（implements）しており、
 * 設計書のクラス図「ToDosDAO」のファイル版です。
 * フェーズ4で、DB版のToDosDAOに切り替えます。
 *
 * データの保存方法（ファイル or データベース）を変更しても、
 * ロジッククラスやメインクラスを修正しなくて済むようにするため、
 * DAOパターンを採用しています。
 * データアクセスの処理をこのクラスに集約することで、保守性が高まります。
 *
 * ファイル形式はCSV:
 * ToDo番号,タスク,期日(yyyy-MM-dd or null),完了フラグ(true/false),メモ
 * 例: 1,買い物をする,2025-06-15,false,牛乳とパン
 *
 * なお、タスク名やメモにカンマ(,)が含まれるとCSVパースが正しく動作しません。
 * この制限はフェーズ4でDB版(ToDosDAO)に移行することで解消されます。
 */
public class ToDoFileDAO implements ToDoDAO {

    /** 保存先ファイル名（固定値として定義） */
    private static final String FILE_NAME = "todos.txt";

    /** ファイルの実際のパス（ディレクトリ + ファイル名） */
    private final Path filePath;

    /**
     * 次に使うToDo番号
     * 新しいToDoを追加するたびにこの値が1増える（自動採番）
     */
    private int nextNo;

    /**
     * デフォルトコンストラクタ
     * カレントディレクトリに todos.txt を作成/読込する
     */
    public ToDoFileDAO() {
        this.filePath = Paths.get(FILE_NAME);
        // 既存データから最大番号を調べ、次の番号を決める
        this.nextNo = calculateNextNo();
    }

    /**
     * ディレクトリ指定コンストラクタ
     * 指定したディレクトリに todos.txt を作成/読込する
     *
     * @param directory ファイルを置くディレクトリのパス
     */
    public ToDoFileDAO(String directory) {
        this.filePath = Paths.get(directory, FILE_NAME);
        this.nextNo = calculateNextNo();
    }

    /**
     * 次のToDo番号を計算する
     *
     * ファイルに既存データがある場合、既に使われている番号と
     * 重複しないように、最大番号+1を次の番号とする必要があります。
     *
     * @return 次に使用すべきToDo番号
     */
    private int calculateNextNo() {
        ArrayList<ToDo> list = findAll();
        int max = 0;
        // 全ToDoをループして最大のToDo番号を見つける
        for (ToDo todo : list) {
            if (todo.getTodoNo() > max) {
                max = todo.getTodoNo();
            }
        }
        // 最大番号 + 1 を返す（データが0件なら1を返す）
        return max + 1;
    }

    /**
     * ToDo全件取得
     *
     * テキストファイルから全行を読み込み、各行をToDoオブジェクトに変換して
     * リストとして返します。ファイルが存在しない場合は空のリストを返します。
     *
     * @return ToDoのリスト（0件の場合は空のArrayList）
     */
    public ArrayList<ToDo> findAll() {
        ArrayList<ToDo> list = new ArrayList<>();

        // ファイルが存在しない場合は空リストを返す（初回起動時など）
        if (!Files.exists(filePath)) {
            return list;
        }

        // try-with-resources: ファイルを開いて処理後に自動で閉じる構文
        // これにより、ファイルの閉じ忘れ（リソースリーク）を防ぐ
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            // 1行ずつ読み込んでループ処理
            while ((line = reader.readLine()) != null) {
                // 空行はスキップ
                if (line.trim().isEmpty())
                    continue;
                // CSVの1行をToDoオブジェクトに変換
                ToDo todo = parseLine(line);
                if (todo != null) {
                    list.add(todo);
                }
            }
        } catch (IOException e) {
            // ファイル読み込みに失敗した場合はエラーメッセージを表示
            System.err.println("ファイル読み込みエラー: " + e.getMessage());
        }
        return list;
    }

    /**
     * ToDo追加（新規作成）
     *
     * ToDoオブジェクトにToDo番号を自動採番し、ファイルの末尾に1行追記します。
     * StandardOpenOption.APPEND を使うことで、既存データを消さずに追記できます。
     *
     * @param todo 追加するToDoオブジェクト（番号は自動設定される）
     * @return 処理結果のメッセージ
     */
    public String createToDo(ToDo todo) {
        // 自動採番: 次の番号をToDoに設定し、カウンターを1増やす
        todo.setTodoNo(nextNo++);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, // ファイルが存在しなければ新規作成
                StandardOpenOption.APPEND)) // 既存内容に追記
        {
            writer.write(formatLine(todo)); // ToDoをCSV形式の文字列に変換して書き込み
            writer.newLine(); // 改行を追加
            return "追加しました。(No." + todo.getTodoNo() + ")";
        } catch (IOException e) {
            return "追加に失敗しました: " + e.getMessage();
        }
    }

    /**
     * ToDo更新（編集）
     *
     * 対象のToDo番号を持つデータを見つけて内容を差し替え、
     * ファイル全体を書き直します。
     * テキストファイルは「途中の1行だけ書き換える」ことができないため、
     * 全データを読み込み→対象を置換→全データを書き直す、という方式を取っています。
     *
     * @param todo 更新するToDoオブジェクト（todoNo で対象を特定する）
     * @return 処理結果のメッセージ
     */
    public String updateToDo(ToDo todo) {
        // まず全件読み込み
        ArrayList<ToDo> list = findAll();
        boolean found = false;

        // リスト内から一致するToDo番号を探して置換
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTodoNo() == todo.getTodoNo()) {
                list.set(i, todo); // 既存のToDoを新しいデータに差し替え
                found = true;
                break;
            }
        }

        // 該当するToDoが見つからなかった場合
        if (!found) {
            return "ToDo No." + todo.getTodoNo() + " が見つかりません。";
        }

        // 更新後のリスト全体をファイルに書き直す
        writeAll(list);
        return "更新しました。(No." + todo.getTodoNo() + ")";
    }

    /**
     * ToDo状態更新（完了/未完了の切り替え）
     *
     * 指定されたToDo番号のcompleted(完了フラグ)だけを変更します。
     * トップ画面のチェックボックスから呼び出される想定です。
     *
     * @param todoNo    変更するToDo番号
     * @param completed 新しい完了状態（true=完了, false=未完了）
     * @return 処理結果のメッセージ
     */
    public String updateCompleted(int todoNo, boolean completed) {
        ArrayList<ToDo> list = findAll();
        for (ToDo todo : list) {
            if (todo.getTodoNo() == todoNo) {
                todo.setCompleted(completed); // 完了状態だけ変更
                writeAll(list); // 全データをファイルに書き直す
                return "状態を更新しました。(No." + todoNo + ")";
            }
        }
        return "ToDo No." + todoNo + " が見つかりません。";
    }

    /**
     * ToDo削除
     *
     * 指定されたToDo番号のデータをリストから除外し、ファイルを書き直します。
     * removeIfメソッドは、条件に合う要素をリストから自動的に削除してくれる便利なメソッドです。
     *
     * @param todoNo 削除するToDo番号
     * @return 処理結果のメッセージ
     */
    public String deleteToDo(int todoNo) {
        ArrayList<ToDo> list = findAll();
        // removeIf: 条件（ラムダ式）に一致する要素を削除、削除できたらtrue
        boolean removed = list.removeIf(todo -> todo.getTodoNo() == todoNo);
        if (!removed) {
            return "ToDo No." + todoNo + " が見つかりません。";
        }
        // 削除後のリストをファイルに書き直す
        writeAll(list);
        return "削除しました。(No." + todoNo + ")";
    }

    /**
     * 全データをファイルに書き出す（内部ヘルパーメソッド）
     *
     * 更新・削除時にファイルの内容を丸ごと書き換える必要があるため、
     * この共通処理をメソッドとして切り出しています。
     * StandardOpenOptionを指定しないことで、既存内容を上書きします。
     *
     * @param list 書き出すToDoリスト
     */
    private void writeAll(ArrayList<ToDo> list) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            for (ToDo todo : list) {
                writer.write(formatLine(todo));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("ファイル書き込みエラー: " + e.getMessage());
        }
    }

    /**
     * CSVの1行をToDoオブジェクトに変換する（内部ヘルパーメソッド）
     *
     * カンマで分割して各フィールドの文字列を取り出し、
     * 各文字列を適切な型（int, LocalDate, booleanなど）に変換してから、
     * ToDoオブジェクトを生成して返します。
     *
     * @param line CSVの1行（例: "1,買い物,2025-06-15,false,牛乳"）
     * @return ToDoオブジェクト（解析失敗時はnull）
     */
    private ToDo parseLine(String line) {
        // カンマで最大5つに分割（メモにカンマが含まれる場合に備えて上限5）
        String[] parts = line.split(",", 5);
        if (parts.length < 5)
            return null;
        try {
            int no = Integer.parseInt(parts[0].trim());
            String task = parts[1].trim();
            // "null"という文字列の場合は期日なし（null）として扱う
            LocalDate dueDate = "null".equals(parts[2].trim()) ? null : LocalDate.parse(parts[2].trim());
            boolean completed = Boolean.parseBoolean(parts[3].trim());
            String memo = "null".equals(parts[4].trim()) ? null : parts[4].trim();
            return new ToDo(no, task, dueDate, completed, memo);
        } catch (Exception e) {
            // 日付形式の不正など、解析に失敗した行はスキップ
            System.err.println("行の解析エラー: " + line);
            return null;
        }
    }

    /**
     * ToDoオブジェクトをCSVの1行に変換する（内部ヘルパーメソッド）
     *
     * ToDoの各フィールドをカンマ区切りの文字列に変換します。
     * nullのフィールドは文字列"null"として保存し、読み込み時に判定します。
     *
     * @param todo 変換するToDoオブジェクト
     * @return CSV形式の文字列（例: "1,買い物,2025-06-15,false,牛乳"）
     */
    private String formatLine(ToDo todo) {
        return String.format("%d,%s,%s,%b,%s",
                todo.getTodoNo(),
                todo.getTask(),
                todo.getDueDate() != null ? todo.getDueDate().toString() : "null",
                todo.isCompleted(),
                todo.getMemo() != null ? todo.getMemo() : "null");
    }
}
