package huylvph30524.poly.lab2.Adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import huylvph30524.poly.lab2.MainActivity;
import huylvph30524.poly.lab2.Modal.Item;
import huylvph30524.poly.lab2.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private Activity activity;
    List<Item> list;
    Context context;
    ExecutorService service;
    OkHttpClient client;
    String ipAddress = "http://172.20.16.1:9999";

    public ItemAdapter(List<Item> list, Context context, ExecutorService service, OkHttpClient client, Activity activity){
        this.list = list;
        this.context = context;
        this.service = service;
        this.client = client;
        this.activity = activity;
    }
    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, int position) {
        Item item = list.get(position);
        holder.tv_name.setText(item.getName());
        holder.tv_price.setText(item.getPrice());
        holder.tv_brand.setText(item.getBrand());
        holder.btn_sua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDialog(item, holder.getAdapterPosition());
            }
        });
        holder.btn_xoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_price, tv_brand;
        Button btn_sua, btn_xoa;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_price = itemView.findViewById(R.id.tv_price);
            tv_brand = itemView.findViewById(R.id.tv_brand);
            btn_sua = itemView.findViewById(R.id.btn_sua);
            btn_xoa = itemView.findViewById(R.id.btn_xoa);
        }
    }

    public void update(final String baseUrl, final String id, final String newName, final int newPrice, final String newBrand, final MainActivity.ResponseListener listener) {
        service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(baseUrl + "/product/updateByid/" + id);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);

                    // Tạo đối tượng JSON chứa thông tin cập nhật
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", newName);
                    jsonObject.put("price", newPrice);
                    jsonObject.put("brand", newBrand);

                    // Gửi dữ liệu cập nhật
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
    public void showAddDialog(Item user, int position) {

        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_item);

        EditText editTextName = dialog.findViewById(R.id.editTextName);
        EditText editTextPrice = dialog.findViewById(R.id.editTextPrice);
        EditText editTextBrand = dialog.findViewById(R.id.editTextBrand);

        Button buttonAdd = dialog.findViewById(R.id.buttonAdd);
        Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
        buttonAdd.setText("Update");

        editTextName.setText(user.getName());
        editTextPrice.setText("" + user.getPrice());
        editTextBrand.setText(user.getBrand());

        buttonAdd.setOnClickListener(v -> {
            if (editTextName.getText().toString().equals("") || editTextPrice.getText().toString().equals("") || editTextBrand.getText().toString().equals("")) {
                Toast.makeText(context, "Hãy nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            }
            if (!editTextName.getText().toString().equals("") && !editTextPrice.getText().toString().equals("") && !editTextBrand.getText().toString().equals("")) {
                String name = editTextName.getText().toString();
                int price = Integer.parseInt(editTextPrice.getText().toString());
                String brand = editTextBrand.getText().toString();

                update(ipAddress, user.getId(), name, price, brand, new MainActivity.ResponseListener() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("ExecuterService", response);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
                list.set(position, new Item(user.getId(), name, price, brand));
                notifyItemChanged(position);
                dialog.dismiss();
            }
            // Xử lý dữ liệu ở đây (ví dụ: lưu vào cơ sở dữ liệu)


        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    public void showDeleteConfirmationDialog(Item user, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa " + user.getName() + " không?");

        builder.setPositiveButton("Xóa", (dialog, which) -> {
            // Xử lý xóa ở đây
            deleteData(user.getId());
            list.remove(position);
            notifyItemRemoved(position);
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void deleteData(final String id) {
        service = Executors.newSingleThreadExecutor();
        service.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Tạo URL cho yêu cầu DELETE
                    String urlDelete = ipAddress + "/product/delByid/" + id;
                    Log.d("ReponseServer", "Check url: " + urlDelete);


                    // Tạo yêu cầu DELETE
                    Request request = new Request.Builder()
                            .url(urlDelete)
                            .delete()
                            .build();

                    // Thực hiện yêu cầu DELETE
                    Response response = client.newCall(request).execute();

                    // Kiểm tra xem yêu cầu có thành công không
                    if (response.isSuccessful()) {
                        Log.d("ReponseServer", "Xóa dữ liệu thành công id: " + id);
                        service.shutdown();
                    } else {
                        Log.d("ReponseServer", "Xóa dữ liệu thất bại");
                        service.shutdown();
                    }

                    // Đóng response
                    response.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("ReponseServer", "Lỗi xảy ra khi xóa dữ liệu: " + e.getMessage());
                }
            }
        });
    }
}
