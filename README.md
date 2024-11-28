# NotePad记事本应用

## **基于NotePad应用实现功能扩展**

### 基本功能实现：

1.NoteList界面中笔记条目增加时间戳显示

2.添加笔记查询功能（根据标题或内容查询）

### 附加功能实现：

1.UI美化——设置主题背景以及选中时背景颜色更改

2.更换背景(编辑笔记时更换背景)

3.笔记排序

### 基本功能具体实现如下

#### 1.NoteList界面中笔记条目增加时间戳显示

在原本的记事本应用中，NoteList界面只有笔记标题的显示，用户可能期望在列表中看到更多的信息，笔记的创建日期、修改时间，因此我们需要在NoteList界面中为为每一条笔记条目添加时间戳，创建时间相当于修改时间，所以最后显示时间戳基于最后一次对笔记的修改时间。

##### 1.1修改布局文件notelist_item.xml

源码中只有一个显示标题的TextView，我们需要再加一个用于显示时间戳的TextView。

修改后的代码如下：

```xml
<LinearLayout
    android:layout_height="match_parent"
    android:layout_width="match_parent"
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical">

    <TextView xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@android:id/title"
        android:layout_width="match_parent"
        android:layout_height="?android:attr/listPreferredItemHeight"
        android:textAppearance="?android:attr/textAppearanceLarge"
        android:gravity="center_vertical"
        android:paddingLeft="5dip"
        android:singleLine="true" />

    <TextView
        android:id="@+id/date"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingLeft="5dip"
        android:singleLine="true"
        android:gravity="center_vertical" />
</LinearLayout>
```

##### 1.2将时间这个数据装填到列表中 修改NoteList.java

首先我们查看程序的数据库结构定义，在NotePadProvider.java中的OnCreate()中我们发现NotePad数据库中已经存在了时间这个信息存储

```java
@Override
       public void onCreate(SQLiteDatabase db) {
           db.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + " ("
                   + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
                   + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
                   + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                   + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                   + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER"
                   + ");");
       }
```

所以接下来，我们需要把时间拿出来装填到列表中。

查看NoteList.java，在当前列的投影PROJECTION中，仅仅只投影出来了ID和每条笔记的标题，因此我们要把关于时间戳的列也投影出来，在PROJECTION中加入修改时间：

```java
private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,//添加修改时间
    };
```

PROJECTION仅仅是定义了需要被取出来的数据列，还需要通过Cursor从数据库中读取出：

```java
Cursor cursor = managedQuery(
            getIntent().getData(),            
            PROJECTION,                       
            null,                             
            null,                             
            NotePad.Notes.DEFAULT_SORT_ORDER  
        );
```

最后还需要用SimpleCursorAdapter进行装填，我们需要将显示列dataColumns和他们的viewIDs加入修改时间这一个属性：

```java
String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE,NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE } ;

int[] viewIDs = { android.R.id.title ,R.id.date};

        SimpleCursorAdapter adapter
            = new SimpleCursorAdapter(
                      this,                             
                      R.layout.noteslist_item,          
                      cursor,                           
                      dataColumns,
                      viewIDs
              );
```

##### 1.3修改时间戳格式

在完成上述步骤后，我们发现显示的并不是我们常见的时间格式，而且一长串数字的时间戳，因此我们需要进行时间戳格式的转化。

新建笔记在NotePadProvider中的insert()方法，修改笔记在NoteEditor中的updateNote()方法，我们需要在这两个方法中进行时间戳的修改。

我们采用`SimpleDateFormat`类进行时间格式化。

NotePadProvider中的insert()方法：

```java
        Long now = Long.valueOf(System.currentTimeMillis());
        //修改 需要将毫秒数转换为时间的形式yy.MM.dd HH:mm:ss
        Date date = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String dateFormat = simpleDateFormat.format(date);//转换为yy.MM.dd HH:mm:ss形式的时间
        // If the values map doesn't contain the creation date, sets the value to the current time.
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, dateFormat);
        }
        // If the values map doesn't contain the modification date, sets the value to the current
        // time.
        if (values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE) == false) {
            values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, dateFormat);
        }
```

NoteEditor中的updateNote()方法：

```java
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        String dateFormat = simpleDateFormat.format(date);
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, dateFormat);
```

##### 1.4运行结果

完成上述步骤后，我们运行这个程序，新建一条笔记，在笔记标题的下面就有时间戳显示了，同时时间的格式也是我们习惯的。

![image-20241127113125826]![image-20241127113135629](https://github.com/hhhappyee/mid_NotePad/blob/master/images/01.png)

修改笔记后，时间也会随之更新修改：

![image-20241127113956339](https://github.com/hhhappyee/mid_NotePad/blob/master/images/02.png)

#### 2.添加笔记查询功能（根据标题或内容查询）

##### 2.1修改list_options_menu.xml文件

要增加笔记查询的功能，我们需要在应用中加入一个查询的入口。在菜单的xml文件——list_options_menu.xml，添加一个用于查询搜索的item：

```xml
<item
        android:id="@+id/menu_search"
        android:title="@string/menu_search"
        android:icon="@drawable/ic_menu_search"
        android:showAsAction="always">
    </item>
```

##### 2.2添加menu_search图标的点击选择事件

在NoteList.java中找到onOptionsItemSelected()方法，往switch语句中添加查询搜索按钮的语句：

```java
case R.id.menu_search:
                Intent intent = new Intent();
                intent.setClass(this, NoteSearch.class);
                this.startActivity(intent);
                return true;
```

##### 2.3添加note_search.xml文件和NoteSearch活动

在case语句中，我们指定点击查询按钮后，跳转到一个新的名为NoteSearch的Activity，因此我们需要编写一个NoteSearch.java，同时我们也要进行搜索页面的布局。

在安卓中有一个用于搜索的控件：SearchView，可以把SearchView和ListView相结合，动态地显示搜索结果。因此**note_search_list.xml**如下：

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" 
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <SearchView
        android:id="@+id/search_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:iconifiedByDefault="false"
        android:queryHint="输入搜索内容..."
        android:layout_alignParentTop="true">
    </SearchView>
    <ListView
        android:id="@android:id/list"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
    </ListView>
</LinearLayout>
```

布局效果如下：

![image-20241127223236524](https://github.com/hhhappyee/mid_NotePad/blob/master/images/03.png)

在NoteSearch.java中，我们需要取到note_search.xml中的ListView和SearchView，并且要动态地显示搜索结果，需要对SearchView文本变化设置监听，使得SearchView中的文本发生变化后，就执行一次查询；为ListView添加监听，使得查询出来的每个item被点击后，能够查看笔记的内容。NoteSearch要继承ListView外还要实现SearchView.OnQueryTextListener接口。

NoteSearch.java:

```java
public class NoteSearch extends ListActivity implements SearchView.OnQueryTextListener  {
    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,//显示修改时间的
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search_list);
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        SearchView searchview = (SearchView)findViewById(R.id.search_view);
        //为查询文本框注册监听器
        searchview.setOnQueryTextListener(NoteSearch.this);
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    @Override
    public boolean onQueryTextChange(String newText) {
        SearchView searchview = (SearchView)findViewById(R.id.search_view);
        searchview.setOnQueryTextListener(NoteSearch.this);
        String selection = NotePad.Notes.COLUMN_NAME_TITLE + " Like ? ";
        String[] selectionArgs = { "%"+newText+"%" };
        Cursor cursor = managedQuery(
                getIntent().getData(),
                PROJECTION,
                selection,
                selectionArgs,
                NotePad.Notes.DEFAULT_SORT_ORDER
        );
        String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE ,  NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE };
        int[] viewIDs = { android.R.id.title , R.id.date };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.noteslist_item,
                cursor,
                dataColumns,
                viewIDs
        );
        setListAdapter(adapter);
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
    }
}
```

最后在AndroidManifest.xml注册NoteSearch的activity

```xml
 <activity
            android:name=".NoteSearch"
            android:label="NoteSearch"
            >
            <intent-filter>
                <action android:name="android.intent.action.NoteSearch" />
                <action android:name="android.intent.action.SEARCH" />
                <action android:name="android.intent.action.SEARCH_LONG_PRESS" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="vnd.android.cursor.dir/vnd.google.note" />
            </intent-filter>
        </activity>
```

##### 2.4运行结果

首先，我们可以在上面的菜单栏看到搜索查询的图标

![image-20241128012341311](https://github.com/hhhappyee/mid_NotePad/blob/master/images/04.png)

查询界面如下：

![image-20241128012620038](https://github.com/hhhappyee/mid_NotePad/blob/master/images/05.png)

![image-20241128012727667](https://github.com/hhhappyee/mid_NotePad/blob/master/images/06.png)

![](https://github.com/hhhappyee/mid_NotePad/blob/master/images/07.gif)

### 附加功能具体实现如下

#### 1.UI美化——设置主题背景以及选中时背景颜色更改

##### 1.1将NotePad变成白色主题

我们进行UI美化之前，需要将NotePad变为白色主题，方便我们后续的操作。

在AndroidManifest.xml中加入一句代码：

```xml
<activity 
          android:name="NotesList" 
          android:label="@string/title_notes_list"   android:theme="@android:style/Theme.Holo.Light">
<activity
            android:name=".NoteSearch"
            android:label="NoteSearch"
            android:theme="@android:style/Theme.Holo.Light"
            >
```

设置后界面如下：

![image-20241128020259621](https://github.com/hhhappyee/mid_NotePad/blob/master/images/08.png)

![image-20241128020050254](https://github.com/hhhappyee/mid_NotePad/blob/master/images/09.png)

##### 1.2对数据库进行操作

要使得每条笔记都可以有自己的背景颜色，需要在数据库中添加字段color来存放每条笔记的背景颜色。

首先，我们需要在系统中提前定好几种颜色，即在NotePad.java中设置颜色编号：

```java
public static final String COLUMN_NAME_BACK_COLOR = "color";
        public static final int DEFAULT_COLOR = 0;
        public static final int YELLOW_COLOR = 1;
        public static final int BLUE_COLOR = 2;
        public static final int GREEN_COLOR = 3;
        public static final int RED_COLOR = 4;
```

接着在创建数据库表的地方NotePadProvider.java中修改创建数据库的语句，添加颜色的字段：

```java
public void onCreate(SQLiteDatabase db) {
           db.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + " ("
                   + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
                   + NotePad.Notes.COLUMN_NAME_TITLE + " TEXT,"
                   + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                   + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                   + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER,"
                   +NotePad.Notes.COLUMN_NAME_BACK_COLOR + " INTEGER"
                   + ");");
       }
```

##### 1.3实例化和设置静态对象的块

我们有一个数据库列 `COLUMN_NAME_BACK_COLOR`，它用来存储了笔记的背景颜色信息，在查询数据库时，可以通过这个映射来访问与背景颜色相关的数据。

在NoteProvider中的static{}进行修改：

```java
sNotesProjectionMap.put(
                NotePad.Notes.COLUMN_NAME_BACK_COLOR,
                NotePad.Notes.COLUMN_NAME_BACK_COLOR);
```

##### 1.4设置创建新的背景的默认背景颜色

在NoteProvider中的insert()进行修改，将默认的背景颜色设成白色：

```java
if (values.containsKey(NotePad.Notes.COLUMN_NAME_BACK_COLOR) == false) {
    values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, NotePad.Notes.DEFAULT_COLOR);
}
```

##### 1.5在PROJECTION添加color属性

执行数据库查询时需要使用投影数组，所以我们在NoteList和NoteSearch中PROJECTION添加：

```java
private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,//显示修改时间的
            NotePad.Notes.COLUMN_NAME_BACK_COLOR,
    };
```

##### 1.6使用bindView将颜色填充到ListView

我们通过新建一个MyCursorAdapter继承SimpleCursorAdapter，这个自定义适配器的主要功能是根据数据库中的背景颜色值动态设置列表项的背景颜色。通过重写 `bindView` 方法来实现，确保每个列表项都能根据存储的颜色值显示正确的背景颜色。如果数据库中缺少颜色值或列不存在，它将使用默认的白色背景。它既可以完成cursor读取的数据库内容填充到item，又能将颜色填充：

MyCursorAdapter.java:

```java
public class MyCursorAdapter extends SimpleCursorAdapter {
    public MyCursorAdapter(Context context, int layout, Cursor c,
                           String[] from, int[] to) {
        super(context, layout, c, from, to);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        // 获取列的索引，并检查它是否存在
        int colorIndex = cursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR);
        if (colorIndex != -1) {
            // 从数据库中读取的先前存入的笔记背景颜色的编码，再设置笔记的背景颜色
            int color = cursor.getInt(colorIndex);
            switch (color) {
                case NotePad.Notes.DEFAULT_COLOR:
                    view.setBackgroundColor(Color.rgb(255, 255, 255));
                    break;
                case NotePad.Notes.YELLOW_COLOR:
                    view.setBackgroundColor(Color.rgb(247, 216, 133));
                    break;
                case NotePad.Notes.BLUE_COLOR:
                    view.setBackgroundColor(Color.rgb(165, 202, 237));
                    break;
                case NotePad.Notes.GREEN_COLOR:
                    view.setBackgroundColor(Color.rgb(161, 214, 174));
                    break;
                case NotePad.Notes.RED_COLOR:
                    view.setBackgroundColor(Color.rgb(244, 149, 133));
                    break;
                default:
                    view.setBackgroundColor(Color.rgb(255, 255, 255));
                    break;
            }
        } else {
            // 如果列不存在，设置一个默认背景颜色
            view.setBackgroundColor(Color.rgb(255, 255, 255)); // 默认白色
        }
    }
}
```

将NoteList和NoteSearch的适配器都修改为MyCursorAdapter：

```java
MyCursorAdapter adapter
    = new MyCursorAdapter(
              this,
              R.layout.noteslist_item,
              cursor, 
              dataColumns,
              viewIDs
      );
```

##### 1.7新建colors.xml和color_select.xml

在选中笔记时笔记的背景颜色会改变

colors.xml:

```xml
<resources>
    <color name="color1">#D5C37FB0</color>
</resources>
```

color_select.xml:

```xml
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@color/color1"
        android:state_pressed="true"/>
</selector>
```

##### 1.8在notelist_item.xml中添加选择器

```xml
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
        android:id="@android:id/title"
        android:layout_width="match_parent"
        android:layout_height="?android:attr/listPreferredItemHeight"
        android:textAppearance="?android:attr/textAppearanceLarge"
        android:gravity="center_vertical"
        android:paddingLeft="5dip"
        android:singleLine="true"
        android:background="@drawable/color_select"/>

    <TextView
        android:id="@+id/date"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingLeft="5dip"
        android:singleLine="true"
        android:gravity="center_vertical"
        android:background="@drawable/color_select"/>
```

##### 1.9运行结果

我们可以看到在选中笔记的时候，笔记的背景颜色会在选中的那一下改变，长按也会改变背景颜色。

![](https://github.com/hhhappyee/mid_NotePad/blob/master/images/10.gif)

#### 2.更换背景(编辑笔记时更换背景)

##### 2.1在NoteEditor.java中增加color属性

因为我们需要在编辑笔记的页面可以显示背景颜色，所以要查询数据库时获取笔记以及其背景颜色信息，就要在 `PROJECTION` 数组中添加了背景颜色的列

```java
private static final String[] PROJECTION =
        new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_TITLE,
            NotePad.Notes.COLUMN_NAME_NOTE,
            NotePad.Notes.COLUMN_NAME_BACK_COLOR
    };
```

##### 2.2在onResume()中增加设置颜色的代码

`onResume()` 方法是在活动从暂停状态恢复到活跃状态时被调用的，无论是从其他活动返回还是从后台恢复到前台。因此，将从数据库读取颜色设置并更新编辑界面背景色的逻辑放在 `onResume()` 方法中是一个很好的选择，这样可以确保无论用户何时返回到编辑界面，背景色都能被正确设置。

```java
if(mCursor!=null){
    mCursor.moveToFirst();
    int x = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR);
    int y = mCursor.getInt(x);
    Log.i("NoteEditor", "color"+y);
    switch (y){
        case NotePad.Notes.DEFAULT_COLOR:
            mText.setBackgroundColor(Color.rgb(255, 255, 255));
            break;
        case NotePad.Notes.YELLOW_COLOR:
            mText.setBackgroundColor(Color.rgb(247, 216, 133));
            break;
        case NotePad.Notes.BLUE_COLOR:
            mText.setBackgroundColor(Color.rgb(165, 202, 237));
            break;
        case NotePad.Notes.GREEN_COLOR:
            mText.setBackgroundColor(Color.rgb(161, 214, 174));
            break;
        case NotePad.Notes.RED_COLOR:
            mText.setBackgroundColor(Color.rgb(244, 149, 133));
            break;
        default:
            mText.setBackgroundColor(Color.rgb(255, 255, 255));
            break;
    }
}
```

##### 2.3增加一个按钮用于更换背景

在editor_options_menu.xml中添加一个更换背景的按钮

```xml
<item android:id="@+id/menu_color"
        android:title="@string/menu_color"
        android:icon="@drawable/ic_menu_color"
        android:showAsAction="always"/>
```

##### 2.4添加menu_color图标的点击选择事件

在NoteEditor.java中增加更换背景图标的选项的执行语句，在onOptionsItemSelected()的switch中添加代码，跳转改变颜色的activity

```java
case R.id.menu_color:
            Intent intent = new Intent(null,mUri);
            intent.setClass(NoteEditor.this,NoteColor.class);
            NoteEditor.this.startActivity(intent);
            break;
```

##### 2.5新建选择背景颜色的布局文件note_color.xml

定义了一个水平排列的 `LinearLayout`，其中包含了五个 `ImageButton`，每个按钮用于选择不同的背景颜色。这些按钮通过 `android:background` 属性设置了不同的颜色，并且每个按钮都绑定了一个点击事件处理方法。

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="horizontal" android:layout_width="match_parent"
    android:layout_height="match_parent">
    <ImageButton
        android:id="@+id/color_white"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="#ffffff"
        android:onClick="white"/>
    <ImageButton
        android:id="@+id/color_yellow"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="#fdef90"
        android:onClick="yellow"/>
    <ImageButton
        android:id="@+id/color_blue"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="#6dc1ed"
        android:onClick="blue"/>
    <ImageButton
        android:id="@+id/color_green"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="#94e49f"
        android:onClick="green"/>
    <ImageButton
        android:id="@+id/color_red"
        android:layout_width="0dp"
        android:layout_height="50dp"
        android:layout_weight="1"
        android:background="#f19696"
        android:onClick="red"/>
</LinearLayout>
```

##### 2.6新建选择颜色的NoteColor的Activity并注册

实现一个颜色选择器的活动，允许用户为笔记选择不同的背景颜色。

```java
public class NoteColor extends Activity {
    private Cursor mCursor;
    private Uri mUri;
    private int color;
    private static final String[] PROJECTION = new String[] {
            NotePad.Notes._ID,
            NotePad.Notes.COLUMN_NAME_BACK_COLOR,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_color);
        mUri = getIntent().getData();
        mCursor = managedQuery(
                mUri,
                PROJECTION,
                null,
                null,
                null
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCursor != null && mCursor.moveToFirst()) {
            int columnIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR);
            if (columnIndex != -1) {
                color = mCursor.getInt(columnIndex);
                Log.i("NoteColor", "before " + color);
            } else {
                Log.e("NoteColor", "Column index not found");
                color = NotePad.Notes.DEFAULT_COLOR; // Default color if column index is not found
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCursor != null) {
            ContentValues values = new ContentValues();
            int columnIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_BACK_COLOR);
            if (columnIndex != -1) {
                values.put(NotePad.Notes.COLUMN_NAME_BACK_COLOR, color);
                getContentResolver().update(mUri, values, null, null);
                int y = mCursor.getInt(columnIndex);
                Log.i("NoteColor", "after " + y);
            } else {
                Log.e("NoteColor", "Column index not found");
            }
        }
    }

    public void white(View view) {
        color = NotePad.Notes.DEFAULT_COLOR;
        finish();
    }

    public void yellow(View view) {
        color = NotePad.Notes.YELLOW_COLOR;
        finish();
    }

    public void blue(View view) {
        color = NotePad.Notes.BLUE_COLOR;
        finish();
    }

    public void green(View view) {
        color = NotePad.Notes.GREEN_COLOR;
        finish();
    }

    public void red(View view) {
        color = NotePad.Notes.RED_COLOR;
        finish();
    }
}
```

最后在AndroidManifest.xml注册NoteColor的activity

```xml
<activity android:name="NoteColor"
    android:theme="@android:style/Theme.Holo.Light.Dialog"
    android:label="Select Color"/>
```

##### 2.7运行结果

运行应用，我们可以在编辑笔记的页面进行对背景颜色的更换，同时背景颜色也会跟随到NoteList界面。

![](https://github.com/hhhappyee/mid_NotePad/blob/master/images/11.gif)

![image-20241128045149658](https://github.com/hhhappyee/mid_NotePad/blob/master/images/12.png)

![image-20241128045213503](https://github.com/hhhappyee/mid_NotePad/blob/master/images/13.png)

#### 3.笔记排序

##### 3.1修改list_options_menu.xml

定义了一个菜单项，其中包含三个子菜单项，用于不同的排序选项。

```xml
<item
        android:id="@+id/menu_sort"
        android:title="@string/menu_sort"
        android:icon="@drawable/ic_menu_sort_by_size"
        android:showAsAction="always" >
        <menu>
            <item
                android:id="@+id/menu_sort1"
                android:title="@string/menu_sort1"/>
            <item
                android:id="@+id/menu_sort2"
                android:title="@string/menu_sort2"/>
            <item
                android:id="@+id/menu_sort3"
                android:title="@string/menu_sort3"/>
        </menu>
    </item>
```

##### 3.2修改NoteList相关代码实现排序逻辑

因为排序会多次使用到cursor，adapter，所以我们将adapter,cursor,dataColumns,viewIDs定义在函数外类内，提高代码的可读性和可维护性。

```java
private MyCursorAdapter adapter;
    private Cursor cursor;
    private String[] dataColumns = { NotePad.Notes.COLUMN_NAME_TITLE ,  NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE } ;
    private int[] viewIDs = { android.R.id.title , R.id.date };
```

在onOptionsItemSelected(MenuItem item)中添加相应的case来相应事件，把Cursor的排序参数变换。

```java
             case R.id.menu_sort1:
                cursor = managedQuery(
                        getIntent().getData(),
                        PROJECTION,
                        null,
                        null,
                        NotePad.Notes._ID
                );
                adapter = new MyCursorAdapter(
                        this,
                        R.layout.noteslist_item,
                        cursor,
                        dataColumns,
                        viewIDs
                );
                setListAdapter(adapter);
                return true;

            case R.id.menu_sort2:
                cursor = managedQuery(
                        getIntent().getData(),
                        PROJECTION,
                        null,
                        null,
                        NotePad.Notes.DEFAULT_SORT_ORDER
                );
                adapter = new MyCursorAdapter(
                        this,
                        R.layout.noteslist_item,
                        cursor,
                        dataColumns,
                        viewIDs
                );
                setListAdapter(adapter);
                return true;
```

##### 3.3运行结果

运行应用，笔记可以按照创建时间、修改时间、颜色进行排序

![](https://github.com/hhhappyee/mid_NotePad/blob/master/images/14.gif)
