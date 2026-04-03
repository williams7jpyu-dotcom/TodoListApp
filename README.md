# ToDo リスト Web アプリケーション

水曜勉強会 演習課題 - ToDoリストアプリ

## 概要

ToDoリストのWebアプリケーションです。  
Java Servlet + MySQL + HTML/CSS/JavaScript で構築されています。

## 機能

- ToDoの**一覧表示**
- ToDoの**新規追加**
- ToDoの**編集**
- ToDoの**削除**
- 完了/未完了の**チェック切替**
- **期限設定**

## 技術構成

| 要素 | 技術 |
|------|------|
| 言語 | Java 17 |
| サーバー | Apache Tomcat 10 (Jakarta Servlet) |
| データベース | MySQL 8.4 |
| フロントエンド | HTML/CSS, JavaScript, JSP |
| 設計パターン | MVC + DAO |
| ビルド | Apache Maven |
| バージョン管理 | Git / GitHub |

## ディレクトリ構成

```
TodoListApp/
├── pom.xml                          # Maven設定
├── README.md
├── docs/                            # 設計書
│   ├── アプリケーション作成手順書ver.2.pdf
│   ├── 01_企画/
│   └── 02_基本設計/
└── src/
    ├── main/
    │   ├── java/com/todoapp/
    │   │   ├── model/               # ToDoモデル
    │   │   ├── logic/               # ビジネスロジック
    │   │   ├── dao/                 # データアクセス
    │   │   └── servlet/             # サーブレット
    │   ├── resources/
    │   └── webapp/
    │       ├── WEB-INF/web.xml
    │       ├── css/
    │       └── js/
    └── test/java/
```

## セットアップ

```bash
# ビルド
mvn clean package

# Tomcat にデプロイ
cp target/TodoListApp.war $CATALINA_HOME/webapps/

# Tomcat 起動
$CATALINA_HOME/bin/startup.bat
```
