package com.mooncloth.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private WebView webView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        );
        dbHelper = new DatabaseHelper(this);

        webView = new WebView(this);
        setContentView(webView);

        configureWebView();
        webView.loadUrl("file:///android_asset/index.html");
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }

    // ======================== DatabaseHelper ========================

    private static final String DB_NAME = "mooncloth.db";
    private static final int DB_VERSION = 3;

    private class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(android.content.Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS products ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "style TEXT,"
                    + "name TEXT,"
                    + "category TEXT,"
                    + "color TEXT,"
                    + "size TEXT,"
                    + "cost REAL DEFAULT 0,"
                    + "price REAL DEFAULT 0,"
                    + "stock INTEGER DEFAULT 0,"
                    + "image TEXT,"
                    + "supplier_id INTEGER,"
                    + "created_at TEXT,"
                    + "status INTEGER DEFAULT 1)");

            db.execSQL("CREATE TABLE IF NOT EXISTS customers ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT,"
                    + "phone TEXT,"
                    + "note TEXT,"
                    + "address TEXT,"
                    + "points INTEGER DEFAULT 0,"
                    + "level INTEGER DEFAULT 1,"
                    + "total_spent REAL DEFAULT 0,"
                    + "purchase_count INTEGER DEFAULT 0,"
                    + "created_at TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS suppliers ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT,"
                    + "category TEXT,"
                    + "phone TEXT,"
                    + "address TEXT,"
                    + "note TEXT,"
                    + "purchase_count INTEGER DEFAULT 0,"
                    + "total_amount REAL DEFAULT 0,"
                    + "created_at TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS sales ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "product_id INTEGER,"
                    + "product_name TEXT,"
                    + "style TEXT,"
                    + "price REAL DEFAULT 0,"
                    + "cost REAL DEFAULT 0,"
                    + "quantity INTEGER DEFAULT 1,"
                    + "total REAL DEFAULT 0,"
                    + "profit REAL DEFAULT 0,"
                    + "customer_id INTEGER,"
                    + "customer_name TEXT,"
                    + "discount REAL DEFAULT 0,"
                    + "note TEXT,"
                    + "payment_method TEXT,"
                    + "created_at TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS purchases ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "product_id INTEGER,"
                    + "product_name TEXT,"
                    + "style TEXT,"
                    + "supplier_id INTEGER,"
                    + "supplier_name TEXT,"
                    + "quantity INTEGER DEFAULT 1,"
                    + "cost REAL DEFAULT 0,"
                    + "total REAL DEFAULT 0,"
                    + "note TEXT,"
                    + "created_at TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS refunds ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "product_id INTEGER,"
                    + "product_name TEXT,"
                    + "sale_id INTEGER,"
                    + "type TEXT,"
                    + "quantity INTEGER DEFAULT 1,"
                    + "amount REAL DEFAULT 0,"
                    + "reason TEXT,"
                    + "exchange_product_id INTEGER,"
                    + "exchange_product_name TEXT,"
                    + "status INTEGER DEFAULT 0,"
                    + "created_at TEXT,"
                    + "updated_at TEXT)");

            insertSampleData(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE products ADD COLUMN supplier_id INTEGER");
                db.execSQL("ALTER TABLE products ADD COLUMN status INTEGER DEFAULT 1");
                db.execSQL("CREATE TABLE IF NOT EXISTS purchases ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "product_id INTEGER,"
                        + "product_name TEXT,"
                        + "style TEXT,"
                        + "supplier_id INTEGER,"
                        + "supplier_name TEXT,"
                        + "quantity INTEGER DEFAULT 1,"
                        + "cost REAL DEFAULT 0,"
                        + "total REAL DEFAULT 0,"
                        + "note TEXT,"
                        + "created_at TEXT)");
                db.execSQL("CREATE TABLE IF NOT EXISTS refunds ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "product_id INTEGER,"
                        + "product_name TEXT,"
                        + "sale_id INTEGER,"
                        + "type TEXT,"
                        + "quantity INTEGER DEFAULT 1,"
                        + "amount REAL DEFAULT 0,"
                        + "reason TEXT,"
                        + "exchange_product_id INTEGER,"
                        + "exchange_product_name TEXT,"
                        + "status INTEGER DEFAULT 0,"
                        + "created_at TEXT,"
                        + "updated_at TEXT)");
            }
            if (oldVersion < 3) {
                db.execSQL("ALTER TABLE sales ADD COLUMN payment_method TEXT");
                db.execSQL("ALTER TABLE sales ADD COLUMN discount REAL DEFAULT 0");
                db.execSQL("ALTER TABLE customers ADD COLUMN level INTEGER DEFAULT 1");
                db.execSQL("ALTER TABLE customers ADD COLUMN points INTEGER DEFAULT 0");
            }
        }

        private void insertSampleData(SQLiteDatabase db) {
            String now = getNow();

            // 3 sample products
            ContentValues p1 = new ContentValues();
            p1.put("style", "MC-001");
            p1.put("name", "经典纯棉T恤");
            p1.put("category", "T恤");
            p1.put("color", "白色");
            p1.put("size", "M");
            p1.put("cost", 30.0);
            p1.put("price", 89.0);
            p1.put("stock", 50);
            p1.put("supplier_id", 1);
            p1.put("created_at", now);
            p1.put("status", 1);
            db.insert("products", null, p1);

            ContentValues p2 = new ContentValues();
            p2.put("style", "MC-002");
            p2.put("name", "修身牛仔裤");
            p2.put("category", "裤子");
            p2.put("color", "深蓝");
            p2.put("size", "L");
            p2.put("cost", 80.0);
            p2.put("price", 199.0);
            p2.put("stock", 30);
            p2.put("supplier_id", 1);
            p2.put("created_at", now);
            p2.put("status", 1);
            db.insert("products", null, p2);

            ContentValues p3 = new ContentValues();
            p3.put("style", "MC-003");
            p3.put("name", "轻薄羽绒服");
            p3.put("category", "外套");
            p3.put("color", "黑色");
            p3.put("size", "XL");
            p3.put("cost", 150.0);
            p3.put("price", 399.0);
            p3.put("stock", 20);
            p3.put("supplier_id", 2);
            p3.put("created_at", now);
            p3.put("status", 1);
            db.insert("products", null, p3);

            // 2 sample customers
            ContentValues c1 = new ContentValues();
            c1.put("name", "张三");
            c1.put("phone", "13800138001");
            c1.put("note", "VIP客户");
            c1.put("address", "北京市朝阳区");
            c1.put("points", 100);
            c1.put("level", 2);
            c1.put("total_spent", 580.0);
            c1.put("purchase_count", 3);
            c1.put("created_at", now);
            db.insert("customers", null, c1);

            ContentValues c2 = new ContentValues();
            c2.put("name", "李四");
            c2.put("phone", "13900139002");
            c2.put("note", "");
            c2.put("address", "上海市浦东新区");
            c2.put("points", 50);
            c2.put("level", 1);
            c2.put("total_spent", 199.0);
            c2.put("purchase_count", 1);
            c2.put("created_at", now);
            db.insert("customers", null, c2);

            // 2 sample suppliers
            ContentValues s1 = new ContentValues();
            s1.put("name", "广州纺织厂");
            s1.put("category", "纺织");
            s1.put("phone", "020-88888001");
            s1.put("address", "广州市白云区");
            s1.put("note", "长期合作供货商");
            s1.put("purchase_count", 5);
            s1.put("total_amount", 15000.0);
            s1.put("created_at", now);
            db.insert("suppliers", null, s1);

            ContentValues s2 = new ContentValues();
            s2.put("name", "杭州服装批发");
            s2.put("category", "服装批发");
            s2.put("phone", "0571-88888002");
            s2.put("address", "杭州市余杭区");
            s2.put("note", "羽绒服专供");
            s2.put("purchase_count", 3);
            s2.put("total_amount", 8000.0);
            s2.put("created_at", now);
            db.insert("suppliers", null, s2);
        }
    }

    // ======================== WebAppInterface ========================

    private String getNow() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    private JSONArray cursorToJson(Cursor cursor) {
        JSONArray array = new JSONArray();
        try {
            String[] columns = cursor.getColumnNames();
            while (cursor.moveToNext()) {
                JSONObject obj = new JSONObject();
                for (int i = 0; i < columns.length; i++) {
                    String col = columns[i];
                    int type = cursor.getType(i);
                    if (type == Cursor.FIELD_TYPE_NULL) {
                        obj.put(col, JSONObject.NULL);
                    } else if (type == Cursor.FIELD_TYPE_INTEGER) {
                        obj.put(col, cursor.getLong(i));
                    } else if (type == Cursor.FIELD_TYPE_FLOAT) {
                        obj.put(col, cursor.getDouble(i));
                    } else if (type == Cursor.FIELD_TYPE_BLOB) {
                        obj.put(col, cursor.getBlob(i));
                    } else {
                        obj.put(col, cursor.getString(i));
                    }
                }
                array.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return array;
    }

    private class WebAppInterface {

        // -------------------- GET methods --------------------

        @JavascriptInterface
        public String getProducts() {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("products", null, null, null, null, null, "id DESC");
            JSONArray result = cursorToJson(cursor);
            cursor.close();
            db.close();
            return result.toString();
        }

        @JavascriptInterface
        public String getCustomers() {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("customers", null, null, null, null, null, "id DESC");
            JSONArray result = cursorToJson(cursor);
            cursor.close();
            db.close();
            return result.toString();
        }

        @JavascriptInterface
        public String getSuppliers() {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("suppliers", null, null, null, null, null, "id DESC");
            JSONArray result = cursorToJson(cursor);
            cursor.close();
            db.close();
            return result.toString();
        }

        @JavascriptInterface
        public String getSales() {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("sales", null, null, null, null, null, "id DESC");
            JSONArray result = cursorToJson(cursor);
            cursor.close();
            db.close();
            return result.toString();
        }

        @JavascriptInterface
        public String getRefunds() {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("refunds", null, null, null, null, null, "id DESC");
            JSONArray result = cursorToJson(cursor);
            cursor.close();
            db.close();
            return result.toString();
        }

        @JavascriptInterface
        public String getPurchases() {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("purchases", null, null, null, null, null, "id DESC");
            JSONArray result = cursorToJson(cursor);
            cursor.close();
            db.close();
            return result.toString();
        }

        // -------------------- GET by ID --------------------

        @JavascriptInterface
        public String getProductById(String id) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("products", null, "id=?", new String[]{id}, null, null, null);
            JSONArray result = cursorToJson(cursor);
            cursor.close();
            db.close();
            if (result.length() > 0) {
                return result.optJSONObject(0).toString();
            }
            return "{}";
        }

        @JavascriptInterface
        public String getCustomerById(String id) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("customers", null, "id=?", new String[]{id}, null, null, null);
            JSONArray result = cursorToJson(cursor);
            cursor.close();
            db.close();
            if (result.length() > 0) {
                return result.optJSONObject(0).toString();
            }
            return "{}";
        }

        @JavascriptInterface
        public String getCustomerPurchases(String customerId) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("sales", null, "customer_id=?", new String[]{customerId}, null, null, "id DESC");
            JSONArray result = cursorToJson(cursor);
            cursor.close();
            db.close();
            return result.toString();
        }

        @JavascriptInterface
        public String getSupplierPurchases(String supplierId) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query("purchases", null, "supplier_id=?", new String[]{supplierId}, null, null, "id DESC");
            JSONArray result = cursorToJson(cursor);
            cursor.close();
            db.close();
            return result.toString();
        }

        // -------------------- STATS --------------------

        @JavascriptInterface
        public String getDailyStats(String date) {
            JSONObject stats = new JSONObject();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try {
                String datePrefix = date + "%";

                // Total sales count and amount for the day
                Cursor salesCursor = db.query("sales",
                        new String[]{"COUNT(*) as cnt", "COALESCE(SUM(total),0) as total_amount", "COALESCE(SUM(profit),0) as total_profit"},
                        "created_at LIKE ?", new String[]{datePrefix}, null, null, null);
                if (salesCursor.moveToFirst()) {
                    stats.put("sales_count", salesCursor.getInt(0));
                    stats.put("sales_amount", salesCursor.getDouble(1));
                    stats.put("sales_profit", salesCursor.getDouble(2));
                }
                salesCursor.close();

                // Total refunds count and amount for the day
                Cursor refundCursor = db.query("refunds",
                        new String[]{"COUNT(*) as cnt", "COALESCE(SUM(amount),0) as total_amount"},
                        "created_at LIKE ?", new String[]{datePrefix}, null, null, null);
                if (refundCursor.moveToFirst()) {
                    stats.put("refund_count", refundCursor.getInt(0));
                    stats.put("refund_amount", refundCursor.getDouble(1));
                }
                refundCursor.close();

                // Total purchases count and amount for the day
                Cursor purchaseCursor = db.query("purchases",
                        new String[]{"COUNT(*) as cnt", "COALESCE(SUM(total),0) as total_amount"},
                        "created_at LIKE ?", new String[]{datePrefix}, null, null, null);
                if (purchaseCursor.moveToFirst()) {
                    stats.put("purchase_count", purchaseCursor.getInt(0));
                    stats.put("purchase_amount", purchaseCursor.getDouble(1));
                }
                purchaseCursor.close();

                // New customers for the day
                Cursor customerCursor = db.query("customers",
                        new String[]{"COUNT(*) as cnt"},
                        "created_at LIKE ?", new String[]{datePrefix}, null, null, null);
                if (customerCursor.moveToFirst()) {
                    stats.put("new_customers", customerCursor.getInt(0));
                }
                customerCursor.close();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            db.close();
            return stats.toString();
        }

        @JavascriptInterface
        public String getMonthlyStats(String year, String month) {
            JSONObject stats = new JSONObject();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try {
                String prefix = year + "-" + (month.length() == 1 ? "0" + month : month) + "%";

                Cursor salesCursor = db.query("sales",
                        new String[]{"COUNT(*) as cnt", "COALESCE(SUM(total),0) as total_amount", "COALESCE(SUM(profit),0) as total_profit"},
                        "created_at LIKE ?", new String[]{prefix}, null, null, null);
                if (salesCursor.moveToFirst()) {
                    stats.put("sales_count", salesCursor.getInt(0));
                    stats.put("sales_amount", salesCursor.getDouble(1));
                    stats.put("sales_profit", salesCursor.getDouble(2));
                }
                salesCursor.close();

                Cursor refundCursor = db.query("refunds",
                        new String[]{"COUNT(*) as cnt", "COALESCE(SUM(amount),0) as total_amount"},
                        "created_at LIKE ?", new String[]{prefix}, null, null, null);
                if (refundCursor.moveToFirst()) {
                    stats.put("refund_count", refundCursor.getInt(0));
                    stats.put("refund_amount", refundCursor.getDouble(1));
                }
                refundCursor.close();

                Cursor purchaseCursor = db.query("purchases",
                        new String[]{"COUNT(*) as cnt", "COALESCE(SUM(total),0) as total_amount"},
                        "created_at LIKE ?", new String[]{prefix}, null, null, null);
                if (purchaseCursor.moveToFirst()) {
                    stats.put("purchase_count", purchaseCursor.getInt(0));
                    stats.put("purchase_amount", purchaseCursor.getDouble(1));
                }
                purchaseCursor.close();

                Cursor customerCursor = db.query("customers",
                        new String[]{"COUNT(*) as cnt"},
                        "created_at LIKE ?", new String[]{prefix}, null, null, null);
                if (customerCursor.moveToFirst()) {
                    stats.put("new_customers", customerCursor.getInt(0));
                }
                customerCursor.close();

            } catch (JSONException e) {
                e.printStackTrace();
            }
            db.close();
            return stats.toString();
        }

        // -------------------- ADD methods --------------------

        @JavascriptInterface
        public String addProduct(String json) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                JSONObject obj = new JSONObject(json);
                ContentValues cv = new ContentValues();
                if (obj.has("style")) cv.put("style", obj.getString("style"));
                if (obj.has("name")) cv.put("name", obj.getString("name"));
                if (obj.has("category")) cv.put("category", obj.getString("category"));
                if (obj.has("color")) cv.put("color", obj.getString("color"));
                if (obj.has("size")) cv.put("size", obj.getString("size"));
                if (obj.has("cost")) cv.put("cost", obj.getDouble("cost"));
                if (obj.has("price")) cv.put("price", obj.getDouble("price"));
                if (obj.has("stock")) cv.put("stock", obj.getInt("stock"));
                if (obj.has("image")) cv.put("image", obj.getString("image"));
                if (obj.has("supplier_id")) cv.put("supplier_id", obj.optInt("supplier_id"));
                cv.put("created_at", getNow());
                cv.put("status", 1);
                long id = db.insert("products", null, cv);
                JSONObject result = new JSONObject();
                result.put("success", id > 0);
                result.put("id", id);
                db.close();
                return result.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                db.close();
                return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @JavascriptInterface
        public String addSale(String json) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                db.beginTransaction();
                JSONObject obj = new JSONObject(json);

                int productId = obj.getInt("product_id");
                int quantity = obj.getInt("quantity");
                double price = obj.optDouble("price", 0);
                double cost = obj.optDouble("cost", 0);
                double discount = obj.optDouble("discount", 0);
                int customerId = obj.optInt("customer_id", 0);
                String customerName = obj.optString("customer_name", "");
                String note = obj.optString("note", "");
                String paymentMethod = obj.optString("payment_method", "现金");

                // Get product info
                Cursor productCursor = db.query("products", new String[]{"name", "style", "stock", "price", "cost"},
                        "id=?", new String[]{String.valueOf(productId)}, null, null, null);
                String productName = "";
                String style = "";
                int currentStock = 0;
                if (productCursor.moveToFirst()) {
                    productName = productCursor.getString(0);
                    style = productCursor.getString(1);
                    currentStock = productCursor.getInt(2);
                    if (price <= 0) price = productCursor.getDouble(3);
                    if (cost <= 0) cost = productCursor.getDouble(4);
                }
                productCursor.close();

                // Check stock
                if (currentStock < quantity) {
                    db.endTransaction();
                    db.close();
                    return "{\"success\":false,\"error\":\"库存不足，当前库存:" + currentStock + "\"}";
                }

                // Update stock
                ContentValues stockUpdate = new ContentValues();
                stockUpdate.put("stock", currentStock - quantity);
                db.update("products", stockUpdate, "id=?", new String[]{String.valueOf(productId)});

                double total = price * quantity - discount;
                double profit = (price - cost) * quantity - discount;

                // Insert sale record
                ContentValues cv = new ContentValues();
                cv.put("product_id", productId);
                cv.put("product_name", productName);
                cv.put("style", style);
                cv.put("price", price);
                cv.put("cost", cost);
                cv.put("quantity", quantity);
                cv.put("total", total);
                cv.put("profit", profit);
                cv.put("customer_id", customerId > 0 ? customerId : null);
                cv.put("customer_name", customerName);
                cv.put("discount", discount);
                cv.put("note", note);
                cv.put("payment_method", paymentMethod);
                cv.put("created_at", getNow());
                long saleId = db.insert("sales", null, cv);

                // Update customer stats
                if (customerId > 0) {
                    // Get current customer data
                    Cursor custCursor = db.query("customers",
                            new String[]{"total_spent", "purchase_count", "points", "level"},
                            "id=?", new String[]{String.valueOf(customerId)}, null, null, null);
                    if (custCursor.moveToFirst()) {
                        double currentSpent = custCursor.getDouble(0);
                        int purchaseCount = custCursor.getInt(1);
                        int currentPoints = custCursor.getInt(2);
                        int level = custCursor.getInt(3);

                        ContentValues custUpdate = new ContentValues();
                        custUpdate.put("total_spent", currentSpent + total);
                        custUpdate.put("purchase_count", purchaseCount + 1);
                        // Points: 1 point per 10 yuan spent
                        int earnedPoints = (int) (total / 10);
                        custUpdate.put("points", currentPoints + earnedPoints);
                        // Level upgrade: every 1000 yuan spent, level +1
                        int newLevel = (int) ((currentSpent + total) / 1000) + 1;
                        if (newLevel > level) {
                            custUpdate.put("level", newLevel);
                        }
                        db.update("customers", custUpdate, "id=?", new String[]{String.valueOf(customerId)});
                    }
                    custCursor.close();
                }

                db.setTransactionSuccessful();
                db.endTransaction();
                JSONObject result = new JSONObject();
                result.put("success", saleId > 0);
                result.put("id", saleId);
                db.close();
                return result.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                db.endTransaction();
                db.close();
                return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @JavascriptInterface
        public String addCustomer(String json) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                JSONObject obj = new JSONObject(json);
                ContentValues cv = new ContentValues();
                if (obj.has("name")) cv.put("name", obj.getString("name"));
                if (obj.has("phone")) cv.put("phone", obj.getString("phone"));
                if (obj.has("note")) cv.put("note", obj.getString("note"));
                if (obj.has("address")) cv.put("address", obj.getString("address"));
                cv.put("points", obj.optInt("points", 0));
                cv.put("level", obj.optInt("level", 1));
                cv.put("total_spent", obj.optDouble("total_spent", 0));
                cv.put("purchase_count", obj.optInt("purchase_count", 0));
                cv.put("created_at", getNow());
                long id = db.insert("customers", null, cv);
                JSONObject result = new JSONObject();
                result.put("success", id > 0);
                result.put("id", id);
                db.close();
                return result.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                db.close();
                return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @JavascriptInterface
        public String addSupplier(String json) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                JSONObject obj = new JSONObject(json);
                ContentValues cv = new ContentValues();
                if (obj.has("name")) cv.put("name", obj.getString("name"));
                if (obj.has("category")) cv.put("category", obj.getString("category"));
                if (obj.has("phone")) cv.put("phone", obj.getString("phone"));
                if (obj.has("address")) cv.put("address", obj.getString("address"));
                if (obj.has("note")) cv.put("note", obj.getString("note"));
                cv.put("purchase_count", obj.optInt("purchase_count", 0));
                cv.put("total_amount", obj.optDouble("total_amount", 0));
                cv.put("created_at", getNow());
                long id = db.insert("suppliers", null, cv);
                JSONObject result = new JSONObject();
                result.put("success", id > 0);
                result.put("id", id);
                db.close();
                return result.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                db.close();
                return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @JavascriptInterface
        public String addRefund(String json) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                db.beginTransaction();
                JSONObject obj = new JSONObject(json);

                int productId = obj.getInt("product_id");
                int saleId = obj.optInt("sale_id", 0);
                String type = obj.optString("type", "退货");
                int quantity = obj.getInt("quantity");
                double amount = obj.optDouble("amount", 0);
                String reason = obj.optString("reason", "");
                int exchangeProductId = obj.optInt("exchange_product_id", 0);
                String exchangeProductName = obj.optString("exchange_product_name", "");

                // Get product info
                Cursor productCursor = db.query("products", new String[]{"name", "style"},
                        "id=?", new String[]{String.valueOf(productId)}, null, null, null);
                String productName = "";
                String style = "";
                if (productCursor.moveToFirst()) {
                    productName = productCursor.getString(0);
                    style = productCursor.getString(1);
                }
                productCursor.close();

                // Restore stock for refund type
                if ("退货".equals(type) || "refund".equalsIgnoreCase(type)) {
                    Cursor stockCursor = db.query("products", new String[]{"stock"},
                            "id=?", new String[]{String.valueOf(productId)}, null, null, null);
                    if (stockCursor.moveToFirst()) {
                        int currentStock = stockCursor.getInt(0);
                        ContentValues stockUpdate = new ContentValues();
                        stockUpdate.put("stock", currentStock + quantity);
                        db.update("products", stockUpdate, "id=?", new String[]{String.valueOf(productId)});
                    }
                    stockCursor.close();
                }

                // Insert refund record
                ContentValues cv = new ContentValues();
                cv.put("product_id", productId);
                cv.put("product_name", productName);
                cv.put("sale_id", saleId > 0 ? saleId : null);
                cv.put("type", type);
                cv.put("quantity", quantity);
                cv.put("amount", amount);
                cv.put("reason", reason);
                cv.put("exchange_product_id", exchangeProductId > 0 ? exchangeProductId : null);
                cv.put("exchange_product_name", exchangeProductName);
                cv.put("status", 0);
                cv.put("created_at", getNow());
                cv.put("updated_at", getNow());
                long refundId = db.insert("refunds", null, cv);

                db.setTransactionSuccessful();
                db.endTransaction();
                JSONObject result = new JSONObject();
                result.put("success", refundId > 0);
                result.put("id", refundId);
                db.close();
                return result.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                db.endTransaction();
                db.close();
                return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @JavascriptInterface
        public String addPurchase(String json) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                db.beginTransaction();
                JSONObject obj = new JSONObject(json);

                int productId = obj.getInt("product_id");
                int supplierId = obj.optInt("supplier_id", 0);
                int quantity = obj.getInt("quantity");
                double cost = obj.optDouble("cost", 0);
                String note = obj.optString("note", "");

                // Get product info
                Cursor productCursor = db.query("products", new String[]{"name", "style"},
                        "id=?", new String[]{String.valueOf(productId)}, null, null, null);
                String productName = "";
                String style = "";
                if (productCursor.moveToFirst()) {
                    productName = productCursor.getString(0);
                    style = productCursor.getString(1);
                }
                productCursor.close();

                // Get supplier info
                String supplierName = "";
                if (supplierId > 0) {
                    Cursor supplierCursor = db.query("suppliers", new String[]{"name"},
                            "id=?", new String[]{String.valueOf(supplierId)}, null, null, null);
                    if (supplierCursor.moveToFirst()) {
                        supplierName = supplierCursor.getString(0);
                    }
                    supplierCursor.close();

                    // Update supplier stats
                    Cursor supStatCursor = db.query("suppliers",
                            new String[]{"purchase_count", "total_amount"},
                            "id=?", new String[]{String.valueOf(supplierId)}, null, null, null);
                    if (supStatCursor.moveToFirst()) {
                        int purchaseCount = supStatCursor.getInt(0);
                        double totalAmount = supStatCursor.getDouble(1);
                        ContentValues supUpdate = new ContentValues();
                        supUpdate.put("purchase_count", purchaseCount + 1);
                        supUpdate.put("total_amount", totalAmount + cost * quantity);
                        db.update("suppliers", supUpdate, "id=?", new String[]{String.valueOf(supplierId)});
                    }
                    supStatCursor.close();
                }

                // Update product stock
                Cursor stockCursor = db.query("products", new String[]{"stock"},
                        "id=?", new String[]{String.valueOf(productId)}, null, null, null);
                if (stockCursor.moveToFirst()) {
                    int currentStock = stockCursor.getInt(0);
                    ContentValues stockUpdate = new ContentValues();
                    stockUpdate.put("stock", currentStock + quantity);
                    stockUpdate.put("cost", cost);
                    db.update("products", stockUpdate, "id=?", new String[]{String.valueOf(productId)});
                }
                stockCursor.close();

                double total = cost * quantity;

                // Insert purchase record
                ContentValues cv = new ContentValues();
                cv.put("product_id", productId);
                cv.put("product_name", productName);
                cv.put("style", style);
                cv.put("supplier_id", supplierId > 0 ? supplierId : null);
                cv.put("supplier_name", supplierName);
                cv.put("quantity", quantity);
                cv.put("cost", cost);
                cv.put("total", total);
                cv.put("note", note);
                cv.put("created_at", getNow());
                long purchaseId = db.insert("purchases", null, cv);

                db.setTransactionSuccessful();
                db.endTransaction();
                JSONObject result = new JSONObject();
                result.put("success", purchaseId > 0);
                result.put("id", purchaseId);
                db.close();
                return result.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                db.endTransaction();
                db.close();
                return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        // -------------------- UPDATE methods --------------------

        @JavascriptInterface
        public String updateProduct(String json) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                JSONObject obj = new JSONObject(json);
                int id = obj.getInt("id");
                ContentValues cv = new ContentValues();
                if (obj.has("style")) cv.put("style", obj.getString("style"));
                if (obj.has("name")) cv.put("name", obj.getString("name"));
                if (obj.has("category")) cv.put("category", obj.getString("category"));
                if (obj.has("color")) cv.put("color", obj.getString("color"));
                if (obj.has("size")) cv.put("size", obj.getString("size"));
                if (obj.has("cost")) cv.put("cost", obj.getDouble("cost"));
                if (obj.has("price")) cv.put("price", obj.getDouble("price"));
                if (obj.has("stock")) cv.put("stock", obj.getInt("stock"));
                if (obj.has("image")) cv.put("image", obj.getString("image"));
                if (obj.has("supplier_id")) cv.put("supplier_id", obj.optInt("supplier_id"));
                if (obj.has("status")) cv.put("status", obj.getInt("status"));
                int rows = db.update("products", cv, "id=?", new String[]{String.valueOf(id)});
                JSONObject result = new JSONObject();
                result.put("success", rows > 0);
                db.close();
                return result.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                db.close();
                return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @JavascriptInterface
        public String updateStock(String id, int newStock) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("stock", newStock);
            int rows = db.update("products", cv, "id=?", new String[]{id});
            JSONObject result = new JSONObject();
            try {
                result.put("success", rows > 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            db.close();
            return result.toString();
        }

        @JavascriptInterface
        public String updateProductImage(String id, String imageData) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("image", imageData);
            int rows = db.update("products", cv, "id=?", new String[]{id});
            JSONObject result = new JSONObject();
            try {
                result.put("success", rows > 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            db.close();
            return result.toString();
        }

        @JavascriptInterface
        public String updateCustomer(String json) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                JSONObject obj = new JSONObject(json);
                int id = obj.getInt("id");
                ContentValues cv = new ContentValues();
                if (obj.has("name")) cv.put("name", obj.getString("name"));
                if (obj.has("phone")) cv.put("phone", obj.getString("phone"));
                if (obj.has("note")) cv.put("note", obj.getString("note"));
                if (obj.has("address")) cv.put("address", obj.getString("address"));
                if (obj.has("points")) cv.put("points", obj.getInt("points"));
                if (obj.has("level")) cv.put("level", obj.getInt("level"));
                if (obj.has("total_spent")) cv.put("total_spent", obj.getDouble("total_spent"));
                if (obj.has("purchase_count")) cv.put("purchase_count", obj.getInt("purchase_count"));
                int rows = db.update("customers", cv, "id=?", new String[]{String.valueOf(id)});
                JSONObject result = new JSONObject();
                result.put("success", rows > 0);
                db.close();
                return result.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                db.close();
                return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @JavascriptInterface
        public String updateSupplier(String json) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                JSONObject obj = new JSONObject(json);
                int id = obj.getInt("id");
                ContentValues cv = new ContentValues();
                if (obj.has("name")) cv.put("name", obj.getString("name"));
                if (obj.has("category")) cv.put("category", obj.getString("category"));
                if (obj.has("phone")) cv.put("phone", obj.getString("phone"));
                if (obj.has("address")) cv.put("address", obj.getString("address"));
                if (obj.has("note")) cv.put("note", obj.getString("note"));
                if (obj.has("purchase_count")) cv.put("purchase_count", obj.getInt("purchase_count"));
                if (obj.has("total_amount")) cv.put("total_amount", obj.getDouble("total_amount"));
                int rows = db.update("suppliers", cv, "id=?", new String[]{String.valueOf(id)});
                JSONObject result = new JSONObject();
                result.put("success", rows > 0);
                db.close();
                return result.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                db.close();
                return "{\"success\":false,\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @JavascriptInterface
        public String updateRefundStatus(String id, int status) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("status", status);
            cv.put("updated_at", getNow());
            int rows = db.update("refunds", cv, "id=?", new String[]{id});
            JSONObject result = new JSONObject();
            try {
                result.put("success", rows > 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            db.close();
            return result.toString();
        }

        // -------------------- DELETE methods --------------------

        @JavascriptInterface
        public String deleteProduct(String id) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rows = db.delete("products", "id=?", new String[]{id});
            JSONObject result = new JSONObject();
            try {
                result.put("success", rows > 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            db.close();
            return result.toString();
        }

        @JavascriptInterface
        public String deleteCustomer(String id) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rows = db.delete("customers", "id=?", new String[]{id});
            JSONObject result = new JSONObject();
            try {
                result.put("success", rows > 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            db.close();
            return result.toString();
        }

        @JavascriptInterface
        public String deleteSupplier(String id) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int rows = db.delete("suppliers", "id=?", new String[]{id});
            JSONObject result = new JSONObject();
            try {
                result.put("success", rows > 0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            db.close();
            return result.toString();
        }

        // -------------------- UTILITY --------------------

        @JavascriptInterface
        public void showToast(String msg) {
            final String message = msg;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
