package co.edu.ue.cryptocurrency;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "CriptoApp.db";
    private static final int DATABASE_VERSION = 2;

    // Tabla de usuarios
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Tabla de portafolio
    private static final String TABLE_PORTFOLIO = "portfolio";
    private static final String COLUMN_PORTFOLIO_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";  // Relaciona el portafolio con el usuario
    public static final String COLUMN_CRYPTO_NAME = "crypto_name"; // Nombre de la criptomoneda
    public static final String COLUMN_CRYPTO_AMOUNT = "crypto_amount"; // Cantidad de la criptomoneda
    public static final String COLUMN_CRYPTO_PRICE = "crypto_price"; // Precio de la criptomoneda en dólares

    // Sentencias SQL para crear las tablas
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_USERNAME + " TEXT NOT NULL," +
            COLUMN_EMAIL + " TEXT UNIQUE NOT NULL," +
            COLUMN_PASSWORD + " TEXT NOT NULL)";

    private static final String CREATE_TABLE_PORTFOLIO = "CREATE TABLE " + TABLE_PORTFOLIO + " (" +
            COLUMN_PORTFOLIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_USER_ID + " INTEGER NOT NULL," +
            COLUMN_CRYPTO_NAME + " TEXT NOT NULL," +
            COLUMN_CRYPTO_AMOUNT + " REAL NOT NULL," +
            COLUMN_CRYPTO_PRICE + " REAL NOT NULL," +
            "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES users(id))";  // Relaciona portafolio con el usuario

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
            // Si la base de datos tiene una versión diferente, eliminamos las tablas existentes y las volvemos a crear
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
            // Consultamos en la tabla de usuarios si el correo existe
            cursor = db.query(TABLE_USERS,
                    new String[]{COLUMN_ID}, // Seleccionamos solo el ID
                    COLUMN_EMAIL + "=?",
                    new String[]{email},
                    null, null, null);

            return cursor != null && cursor.getCount() > 0;  // Si el cursor tiene resultados, el email ya existe
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
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_PASSWORD, password);

            // Insertamos los datos del usuario en la tabla de usuarios
            long result = db.insert(TABLE_USERS, null, values);
            return result != -1;  // Si el resultado es -1, hubo un error
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al registrar usuario", e);
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
                    new String[]{COLUMN_ID},  // Seleccionamos solo el ID
                    COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                    new String[]{email, password},
                    null, null, null);

            return cursor != null && cursor.getCount() > 0;  // Si el cursor tiene resultados, el usuario existe
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
    public boolean addToPortfolio(int userId, String cryptoName, double amount, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_CRYPTO_NAME, cryptoName);
        values.put(COLUMN_CRYPTO_AMOUNT, amount);
        values.put(COLUMN_CRYPTO_PRICE, price);

        long result = db.insert(TABLE_PORTFOLIO, null, values);
        db.close();
        return result != -1;  // Si el resultado es -1, hubo un error
    }

    // Método para obtener todas las criptomonedas del portafolio de un usuario
    public Cursor getPortfolio(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PORTFOLIO,
                new String[]{COLUMN_PORTFOLIO_ID, COLUMN_CRYPTO_NAME, COLUMN_CRYPTO_AMOUNT, COLUMN_CRYPTO_PRICE},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);
    }

    // Método para actualizar la cantidad de una criptomoneda en el portafolio
    public boolean updatePortfolio(int portfolioId, double newAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CRYPTO_AMOUNT, newAmount);

        int rowsAffected = db.update(TABLE_PORTFOLIO, values, COLUMN_PORTFOLIO_ID + "=?",
                new String[]{String.valueOf(portfolioId)});
        db.close();
        return rowsAffected > 0;  // Si se actualizó al menos una fila, el update fue exitoso
    }

    // Método para eliminar una criptomoneda del portafolio
    public boolean removeFromPortfolio(int portfolioId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_PORTFOLIO, COLUMN_PORTFOLIO_ID + "=?",
                new String[]{String.valueOf(portfolioId)});
        db.close();
        return rowsDeleted > 0;  // Si se eliminó al menos una fila, el delete fue exitoso
    }
}
