# basic-spring-boot-app

[![Build](https://github.com/TakuyaFukumura/basic-spring-boot-app/workflows/Build/badge.svg)](https://github.com/TakuyaFukumura/basic-spring-boot-app/actions?query=branch%3Amain)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6.3-blue)](https://maven.apache.org/)

SpringBootアプリ開発の元となるリポジトリ

## 資料
- https://spring.pleiades.io/spring-boot/docs/current/reference/html/getting-started.html

## Docker開発環境セットアップ

### 前提条件
- Docker
- Docker Compose

### 起動手順
1. リポジトリをクローン
    ```bash
    git clone https://github.com/TakuyaFukumura/basic-spring-boot-app.git
    ```
    ```bash
    cd basic-spring-boot-app
    ```
2. Docker Composeでアプリケーションを起動
    ```bash
    docker compose up --build
    ```
3. ブラウザでアクセス

    http://localhost:8080

4. H2データベースコンソールへのアクセス（開発用）

    http://localhost:8080/h2-console

5. ヘルスチェックエンドポイント

    http://localhost:8080/actuator/health

### Docker コマンド

#### アプリケーションの停止
```bash
docker compose down
```

#### ログの確認
```bash
docker compose logs -f app
```

#### イメージの再ビルド
```bash
docker compose build --no-cache
```

## 従来の起動方法（Docker不使用）

### 起動
```bash
./mvnw spring-boot:run
```

### コンパイルと実行
```bash
./mvnw clean package
```
```bash
java -jar target/myproject.jar
```

## 静的解析ツール（SpotBugs）

### SpotBugsとは
SpotBugsは、Javaコードの潜在的なバグや問題を検出する静的解析ツールです。
コードのコンパイル後のバイトコードを解析し、一般的なバグパターンや問題のあるコーディングパターンを発見します。

### SpotBugsの実行

#### 基本的な解析の実行
```bash
./mvnw spotbugs:spotbugs
```

#### 解析結果の確認とビルド時のチェック
```bash
./mvnw spotbugs:check
```

#### HTMLレポートの確認
解析実行後、次のファイルでHTMLレポートを確認できます： target/site/spotbugs.html

各OSでのコマンド例:
- **Windows**:
    ```bash
    start target/site/spotbugs.html
    ```
- **macOS**:
    ```bash
    open target/site/spotbugs.html
    ```
- **Linux**:
    ```bash
    xdg-open target/site/spotbugs.html
    ```

### SpotBugsの設定

#### 解析対象の設定
- `spotbugs-include.xml`: 解析対象のパッケージやクラスを指定
- `spotbugs-exclude.xml`: 解析から除外するパッケージやクラスを指定

#### 解析レベルの設定
- **Effort**: Max（最大）- より詳細な解析を実行
- **Threshold**: Low（低）- より多くの問題を検出

### Docker環境での実行
Docker環境でもSpotBugsを実行できます：
```bash
docker compose exec app ./mvnw spotbugs:spotbugs
```
