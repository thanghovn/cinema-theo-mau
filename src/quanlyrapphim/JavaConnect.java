package quanlyrapphim;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class JavaConnect {

    static Connection conn = null;

    // Cả chương trình dùng chung 1 kết nối. Nếu mỗi màn hình mở 1 kết nối riêng,
    // SQLite sẽ báo lỗi "database is locked" khi 2 kết nối cùng đọc/ghi.
    public static Connection ConnectDB() {
        if (conn != null) {
            return conn;
        }
        try {
            Class.forName("org.sqlite.JDBC");
            // Đường dẫn tương đối: tính từ thư mục project (NetBeans chạy ở đó)
            conn = DriverManager.getConnection("jdbc:sqlite:database/QuanLyRapPhim.sqlite");
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, e);
            return null;
        }
    }

    // Đổ kết quả câu SELECT vào JTable (thay cho DbUtils của thư viện rs2xml)
    public static void loadTable(JTable table, ResultSet rs) throws SQLException {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        int soCot = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= soCot; i++) {
            model.addColumn(rs.getMetaData().getColumnName(i));
        }
        while (rs.next()) {
            Object[] dong = new Object[soCot];
            for (int i = 1; i <= soCot; i++) {
                dong[i - 1] = rs.getObject(i);
            }
            model.addRow(dong);
        }
        table.setModel(model);
    }

    // Kiểm tra chuỗi có đúng dạng dd/MM/yyyy và là ngày có thật không
    public static boolean laNgay(String s) {
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        f.setLenient(false);
        try {
            f.parse(s);
            return s.length() == 10;
        } catch (ParseException e) {
            return false;
        }
    }
}
