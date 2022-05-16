package si.uni_lj.fe.tnuv.smartslippers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " +
                "fname" + " TEXT," +
                "lname" + " TEXT," +
                "email" + " TEXT," +
                "password" + " TEXT" + ")")

        // we are calling sqlite
        // method for executing our query
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, p1: Int, p2: Int) {
        // this method is to check if table already exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    // This method is for adding data in our database
    fun addName(fname : String, lname : String, email : String, password : String ){

        // below we are creating
        // a content values variable
        // ContentValues() - creates empty set of values
        val values = ContentValues()

        // we are inserting our values
        // in the form of key-value pair
        // to pass key and value we use put(key,value)
        values.put("fname", fname)
        values.put("lname", lname)
        values.put("email", email)
        values.put("password", password)

        // here we are creating a
        // writable variable of
        // our database as we want to
        // insert value in our database
        val db = this.writableDatabase

        // all values are inserted into database
        db.insert(TABLE_NAME, null, values)

        // at last we are
        // closing our database
        db.close()
    }

    // below method is to get
    // all data from our database
    fun count(): Cursor? {

        // here we are creating a readable
        // variable of our database
        // as we want to read value from it
        val db = this.readableDatabase

        // below code returns a cursor to
        // read data from the database
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null)

    }

    @SuppressLint("Range")
    fun getName(id: Long): Pair<String, String> {
        var email = "not found"
        var password = "not found"
        val db = this.writableDatabase
        val whereclause = "ID=?"
        val whereargs = arrayOf(id.toString())
        val csr: Cursor = db.query(TABLE_NAME, null, whereclause, whereargs, null, null, null)
        if (csr.moveToFirst()) {
            email = csr.getString(csr.getColumnIndex(EMAIL_COl))
            password = csr.getString(csr.getColumnIndex(PASSWORD_COl))
        }
        return Pair(email, password)
    }

    companion object{
        // here we have defined variables for our database

        // below is variable for database name
        private val DATABASE_NAME = "SIGNUP"

        // below is the variable for database version
        private val DATABASE_VERSION = 1

        // below is the variable for table name
        val TABLE_NAME = "gfg_table1"

        // below is the variable for id column
        val ID_COL = "id"

        // below is the variable for name column
        val FNAME_COl = "fname"

        // below is the variable for age column
        val LNAME_COl = "lname"

        val EMAIL_COl = "email"

        val PASSWORD_COl = "password"
    }
}