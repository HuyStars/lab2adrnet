package huylvph30524.poly.lab2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import huylvph30524.poly.lab2.Adapter.ItemAdapter;
import huylvph30524.poly.lab2.Modal.Item;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {
    ExecutorService service;

    public OkHttpClient client = new OkHttpClient();
    public String apAddres = "http://172.20.16.1:9999";

    RecyclerView listItem;
    FloatingActionButton fabAdd;
    ItemAdapter adapter;
    List<Item> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        service = Executors.newCachedThreadPool();
        listItem = findViewById(R.id.listItem);
        fabAdd = findViewById(R.id.fab);
        list = new ArrayList<>();

        GetData();
        fabAdd.setOnClickListener(view -> {
            showAddDialog();
        });
    }

    private JSONArray callAPIGetData(String urlString) throws IOException {
        // Tạo URL từ đường dẫn đã cho
        URL url = new URL(urlString);
        // Mở kết nối
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            // Đọc dữ liệu từ kết nối
            InputStream in = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            // Chuyển dữ liệu đọc được thành một JSONArray
            return new JSONArray(result.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            // Đóng kết nối
            urlConnection.disconnect();
        }
    }
    public void postData(final String urlString, final String name, final int price, final String brand, final ResponseListener listener) {
        service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Tạo đối tượng JSON từ dữ liệu
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", name);
                    jsonObject.put("price", price);
                    jsonObject.put("brand", brand);

                    // Gửi dữ liệu
                    DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
                    outStream.writeBytes(jsonObject.toString());
                    outStream.flush();
                    outStream.close();

                    // Đọc phản hồi từ server
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    // Gửi phản hồi cho listener
                    if (listener != null) {
                        listener.onResponse(response.toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // Gửi lỗi cho listener
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        });
    }
    public interface ResponseListener {
        void onResponse(String response);

        void onError(Exception e);
    }
    public class MyRunable implements Runnable {
        String name;

        public MyRunable(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            try {
                Log.d("ExecuterService", name + " dang chạy");
                Thread.sleep(5000);
                Log.d("ExecuterService", name + " chết");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
    public class MyCallable implements Callable<JSONArray> {

        @Override
        public JSONArray call() throws Exception {
            JSONArray jsonArray = callAPIGetData(apAddres + "/product/getall");
            return jsonArray;
        }
    }
    public void showAddDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_item);

        EditText editTextName = dialog.findViewById(R.id.editTextName);
        EditText editTextPrice = dialog.findViewById(R.id.editTextPrice);
        EditText editTextBrand = dialog.findViewById(R.id.editTextBrand);

        Button buttonAdd = dialog.findViewById(R.id.buttonAdd);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);

        buttonAdd.setOnClickListener(v -> {
            if (editTextName.getText().toString().equals("") || editTextPrice.getText().toString().equals("") || editTextBrand.getText().toString().equals("")) {
                Toast.makeText(this, "Hãy nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
            if (!editTextName.getText().toString().equals("") && !editTextPrice.getText().toString().equals("") && !editTextBrand.getText().toString().equals("")) {
                String name = editTextName.getText().toString();
                int price = Integer.parseInt(editTextPrice.getText().toString());
                String brand = editTextBrand.getText().toString();

                postData(apAddres + "/product/post",
                        name, price, brand, new ResponseListener() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("ExecuterService", response);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });
                list.clear();
                GetData();
                dialog.dismiss();
            }
            // Xử lý dữ liệu ở đây (ví dụ: lưu vào cơ sở dữ liệu)


        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    public void GetData() {
        try {
            Future<JSONArray> future = service.submit(new MyCallable());
            if (future.get() != null) {
                Log.d("ExecuterService", future.get().toString());
                JSONArray jsonArray = null; // jsonString là chuỗi JSON bạn đã cung cấp
                try {
                    jsonArray = new JSONArray(future.get().toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String id = jsonObject.getString("_id");
                        String name = jsonObject.getString("name");
                        int price = jsonObject.getInt("price");
                        String brand = jsonObject.getString("brand");

                        Item product = new Item(id, name, price, brand);
                        list.add(product);
                    }
                    adapter = new ItemAdapter(list, getApplicationContext(), service, client, MainActivity.this);
                    listItem.setAdapter(adapter);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        service.shutdown();
    }
}