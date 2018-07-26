### table-creator-android
=========================

An Android helper class to manage table creation based on model/pojo class

Setup
-----
#### Gradle

If you are using the Gradle build system, simply add the following dependency in your `build.gradle` file:

```groovy
dependencies {
    implementation 'org.chalup.microorm:microorm:0.8.0'
    implementation 'com.github.ranaparamveer:table-creator-android:v1.0.4'
}
```


And add : **maven { url 'https://jitpack.io' }** to project level gradle file under allprojects as:


```
allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
}
```

Usage
-----

Open your database in writable mode and pass on the database object to TableCreator constructor along with table name and class for Model to use as reference for column names

```java
        DatabaseHelper openHelper = new DatabaseHelper(this);
        SQLiteDatabase sqLiteDatabase = openHelper.getWritableDatabase();
        new TableCreator().createTable(sqLiteDatabase, "MyTable", MyModel.class);
```

This method returns true if table already exist or if it is created successfully and false otherwise.


To add a field as colum, attach ```@Column``` annotation and to set any field as primary, use ```@PrimaryKey``` as annotation. e.g:

```
    @Column("pk")
    @PrimaryKey
    private int primaryKey;
    @Column("empID")
    int id;
    @Column("name")
    String name;
    @Column("isPresent")
    boolean isPresent;
    @Column("salary")
    float salaryInLacs;
    ```