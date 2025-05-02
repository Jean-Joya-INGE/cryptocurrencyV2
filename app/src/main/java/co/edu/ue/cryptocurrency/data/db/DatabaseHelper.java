package co.edu.ue.cryptocurrency.data.db;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "CriptoApp.db";
    private static final int DATABASE_VERSION = 3;

    // Tabla de usuarios
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Tabla de portafolio
    private static final String TABLE_PORTFOLIO = "portfolio";
    public static final String COLUMN_PORTFOLIO_ID = "id";
    public static final String COLUMN_USER_ID = "user_id"; // Relaciona el portafolio con el usuario
    public static final String COLUMN_CRYPTO_ID = "crypto_id"; // ID de la criptomoneda (ej: bitcoin)
    public static final String COLUMN_CRYPTO_NAME = "crypto_name"; // Nombre de la criptomoneda
    public static final String COLUMN_CRYPTO_SYMBOL = "crypto_symbol"; // Símbolo de la criptomoneda
    public static final String COLUMN_CRYPTO_AMOUNT = "crypto_amount"; // Cantidad de la criptomoneda
    public static final String COLUMN_CRYPTO_PRICE = "crypto_price"; // Precio de compra de la criptomoneda
    public static final String COLUMN_PURCHASE_DATE = "purchase_date"; // Fecha de compra

    // Sentencias SQL para crear las tablas
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_USERNAME + " TEXT NOT NULL," +
            COLUMN_EMAIL + " TEXT UNIQUE NOT NULL," +
            COLUMN_PASSWORD + " TEXT NOT NULL)";

    private static final String CREATE_TABLE_PORTFOLIO = "CREATE TABLE " + TABLE_PORTFOLIO + " (" +
            COLUMN_PORTFOLIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_USER_ID + " INTEGER NOT NULL," +
            COLUMN_CRYPTO_ID + " TEXT NOT NULL," +
            COLUMN_CRYPTO_NAME + " TEXT NOT NULL," +
            COLUMN_CRYPTO_SYMBOL + " TEXT NOT NULL," +
            COLUMN_CRYPTO_AMOUNT + " REAL NOT NULL," +
            COLUMN_CRYPTO_PRICE + " REAL NOT NULL," +
            COLUMN_PURCHASE_DATE + " TEXT NOT NULL," +
            "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES users(id))"; // Relaciona portafolio con el usuario

    // Constructor de la clase
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Se llama cuando se crea la base de datos por primera vez
    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Crear las tablas de usuarios y portafolio
            db.execSQL(CREATE_TABLE_USERS);
            db.execSQL(CREATE_TABLE_PORTFOLIO);
            Log.d("DatabaseHelper", "Tablas creadas exitosamente");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error creando tablas", e);
            throw e;
        }
    }

    // Se llama cuando la versión de la base de datos cambia
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            // Si la base de datos tiene una versión diferente, eliminamos las tablas
            // existentes y las volvemos a crear
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PORTFOLIO);
            onCreate(db);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al actualizar la base de datos", e);
            throw e;
        }
    }

    // Método para verificar si un correo electrónico ya está registrado
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            Log.d("DatabaseHelper", "Verificando si existe el email: " + email);

            // Consultamos en la tabla de usuarios si el correo existe
            cursor = db.query(TABLE_USERS,
                    new String[] { COLUMN_ID }, // Seleccionamos solo el ID
                    COLUMN_EMAIL + "=?",
                    new String[] { email },
                    null, null, null);

            boolean exists = cursor != null && cursor.getCount() > 0;
            Log.d("DatabaseHelper", "¿Email existe en BD?: " + exists);

            return exists; // Si el cursor tiene resultados, el email ya existe
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al verificar email", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Método para registrar un nuevo usuario
    public boolean registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Log.d("DatabaseHelper", "Intentando registrar usuario: " + username + ", Email: " + email);

            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_PASSWORD, password);

            // Insertamos los datos del usuario en la tabla de usuarios
            long result = db.insert(TABLE_USERS, null, values);
            boolean success = result != -1;

            Log.d("DatabaseHelper",
                    "Resultado del registro: " + (success ? "Éxito" : "Fallido") + " (ID: " + result + ")");

            return success; // Si el resultado es -1, hubo un error
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al registrar usuario", e);
            e.printStackTrace(); // Imprime la traza completa del error
            return false;
        } finally {
            db.close();
        }
    }

    // Método para verificar las credenciales de un usuario (inicio de sesión)
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Consultamos si existe un usuario con el correo y la contraseña proporcionados
            cursor = db.query(TABLE_USERS,
                    new String[] { COLUMN_ID }, // Seleccionamos solo el ID
                    COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                    new String[] { email, password },
                    null, null, null);

            return cursor != null && cursor.getCount() > 0; // Si el cursor tiene resultados, el usuario existe
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al verificar usuario", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Método para agregar una criptomoneda al portafolio
    public boolean addToPortfolio(int userId, String cryptoId, String cryptoName, String cryptoSymbol,
            double amount, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Crear fecha actual en formato texto
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String purchaseDate = dateFormat.format(new Date());

            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, userId);
            values.put(COLUMN_CRYPTO_ID, cryptoId);
            values.put(COLUMN_CRYPTO_NAME, cryptoName);
            values.put(COLUMN_CRYPTO_SYMBOL, cryptoSymbol);
            values.put(COLUMN_CRYPTO_AMOUNT, amount);
            values.put(COLUMN_CRYPTO_PRICE, price);
            values.put(COLUMN_PURCHASE_DATE, purchaseDate);

            long result = db.insert(TABLE_PORTFOLIO, null, values);
            return result != -1; // Si el resultado es -1, hubo un error
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al agregar criptomoneda al portafolio", e);
            return false;
        } finally {
            db.close();
        }
    }

    // Método para obtener todas las criptomonedas del portafolio de un usuario
    public Cursor getPortfolio(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PORTFOLIO,
                null, // Seleccionamos todas las columnas
                COLUMN_USER_ID + "=?",
                new String[] { String.valueOf(userId) },
                null, null, null);
    }

    // Método para actualizar una criptomoneda en el portafolio
    public boolean updatePortfolio(int portfolioId, double newAmount, double newPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CRYPTO_AMOUNT, newAmount);
            values.put(COLUMN_CRYPTO_PRICE, newPrice);

            // Actualizar fecha de compra
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            values.put(COLUMN_PURCHASE_DATE, dateFormat.format(new Date()));

            int rowsAffected = db.update(TABLE_PORTFOLIO, values, COLUMN_PORTFOLIO_ID + "=?",
                    new String[] { String.valueOf(portfolioId) });
            return rowsAffected > 0; // Si se actualizó al menos una fila, el update fue exitoso
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al actualizar criptomoneda", e);
            return false;
        } finally {
            db.close();
        }
    }

    // Método para eliminar una criptomoneda del portafolio
    public boolean removeFromPortfolio(int portfolioId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int rowsDeleted = db.delete(TABLE_PORTFOLIO, COLUMN_PORTFOLIO_ID + "=?",
                    new String[] { String.valueOf(portfolioId) });
            return rowsDeleted > 0; // Si se eliminó al menos una fila, el delete fue exitoso
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al eliminar criptomoneda", e);
            return false;
        } finally {
            db.close();
        }
    }

    // Método para obtener el ID de un usuario a partir de su email
    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS,
                    new String[] { COLUMN_ID },
                    COLUMN_EMAIL + "=?",
                    new String[] { email },
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
                return cursor.getInt(columnIndex);
            }
            return -1; // Usuario no encontrado
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al obtener ID de usuario", e);
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
