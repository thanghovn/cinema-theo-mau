package quanlyrapphim;

import java.sql.*;
import javax.swing.*;

public class TaiKhoan extends JInternalFrame {

    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;

    JPasswordField txtMatKhauCu = new JPasswordField();
    JPasswordField txtMatKhauMoi = new JPasswordField();
    JPasswordField txtNhapLai = new JPasswordField();
    JButton btnTaiKhoanLuu = new JButton("Đổi mật khẩu");

    JTextField txtTenMoi = new JTextField();
    JPasswordField txtPassMoi = new JPasswordField();
    JButton btnThemTaiKhoan = new JButton("Thêm tài khoản");
    JComboBox<String> cboQuyen = new JComboBox<>(new String[]{"Nhân viên", "Admin phụ"});
    JButton btnCapQuyen = new JButton("Cấp quyền");
    JButton btnTuocQuyen = new JButton("Tước quyền");
    JTable jTableTaiKhoan = new JTable();

    public TaiKhoan() {
        initComponents();
        conn = JavaConnect.ConnectDB();
        if (Login.admin >= 1) {
            updateTableTaiKhoan();
        }
    }

    private void initComponents() {
        setTitle("Tài khoản");
        setBounds(0, 0, 960, 580);
        setLayout(null);

        JLabel lb1 = new JLabel("ĐỔI MẬT KHẨU");
        lb1.setBounds(20, 15, 200, 25);
        add(lb1);
        String[] nhan = {"Mật khẩu cũ:", "Mật khẩu mới:", "Nhập lại:"};
        JPasswordField[] o = {txtMatKhauCu, txtMatKhauMoi, txtNhapLai};
        for (int i = 0; i < o.length; i++) {
            JLabel lb = new JLabel(nhan[i]);
            lb.setBounds(20, 50 + 38 * i, 120, 25);
            add(lb);
            o[i].setBounds(140, 50 + 38 * i, 200, 25);
            add(o[i]);
        }
        btnTaiKhoanLuu.setBounds(140, 170, 140, 30);
        add(btnTaiKhoanLuu);
        btnTaiKhoanLuu.addActionListener(e -> btnTaiKhoanLuuActionPerformed());

        // Phần thêm tài khoản, cấp/tước quyền chỉ admin (chính hoặc phụ) mới thấy
        if (Login.admin >= 1) {
            JLabel lb2 = new JLabel("THÊM TÀI KHOẢN (ADMIN)");
            lb2.setBounds(450, 15, 250, 25);
            add(lb2);
            JLabel lbTen = new JLabel("Tên đăng nhập:");
            lbTen.setBounds(450, 50, 110, 25);
            add(lbTen);
            txtTenMoi.setBounds(560, 50, 200, 25);
            add(txtTenMoi);
            JLabel lbPass = new JLabel("Mật khẩu:");
            lbPass.setBounds(450, 88, 110, 25);
            add(lbPass);
            txtPassMoi.setBounds(560, 88, 200, 25);
            add(txtPassMoi);
            btnThemTaiKhoan.setBounds(560, 126, 140, 30);
            add(btnThemTaiKhoan);

            JLabel lb3 = new JLabel("CẤP / TƯỚC QUYỀN (chọn 1 tài khoản trong bảng)");
            lb3.setBounds(450, 170, 400, 25);
            add(lb3);
            cboQuyen.setBounds(450, 200, 120, 25);
            add(cboQuyen);
            btnCapQuyen.setBounds(580, 198, 110, 30);
            add(btnCapQuyen);
            btnTuocQuyen.setBounds(700, 198, 110, 30);
            add(btnTuocQuyen);

            JScrollPane scroll = new JScrollPane(jTableTaiKhoan);
            scroll.setBounds(450, 240, 480, 290);
            add(scroll);
            btnThemTaiKhoan.addActionListener(e -> btnThemTaiKhoanActionPerformed());
            // combo: vị trí 0 = nhân viên (0), vị trí 1 = admin phụ (1)
            btnCapQuyen.addActionListener(e -> doiQuyen(cboQuyen.getSelectedIndex()));
            btnTuocQuyen.addActionListener(e -> doiQuyen(-1));
        }
    }

    // Không hiện cột mật khẩu ra bảng
    private void updateTableTaiKhoan() {
        try {
            pst = conn.prepareStatement("select username, case admin when 2 then 'Admin chính' when 1 then 'Admin phụ'"
                    + " when 0 then 'Nhân viên' else 'Chưa cấp quyền' end as quyen from login");
            rs = pst.executeQuery();
            JavaConnect.loadTable(jTableTaiKhoan, rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void btnTaiKhoanLuuActionPerformed() {
        String cu = new String(txtMatKhauCu.getPassword());
        String moi = new String(txtMatKhauMoi.getPassword());
        String lai = new String(txtNhapLai.getPassword());
        if (moi.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Mật khẩu mới không được để trống");
            return;
        }
        if (!moi.equals(lai)) {
            JOptionPane.showMessageDialog(null, "Nhập lại mật khẩu không khớp");
            return;
        }
        try {
            String sql = "update login set password=? where username=? and password=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, moi);
            pst.setString(2, Login.useNameLogin);
            pst.setString(3, cu);
            int n = pst.executeUpdate();
            if (n == 0) {
                JOptionPane.showMessageDialog(null, "Mật khẩu cũ không đúng");
            } else {
                JOptionPane.showMessageDialog(null, "Đổi mật khẩu thành công");
                txtMatKhauCu.setText("");
                txtMatKhauMoi.setText("");
                txtNhapLai.setText("");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void btnThemTaiKhoanActionPerformed() {
        String ten = txtTenMoi.getText().trim();
        String pass = new String(txtPassMoi.getPassword());
        if (ten.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Phải nhập tên đăng nhập và mật khẩu");
            return;
        }
        try {
            String sql = "Insert into login(username,password,admin) values (?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, ten);
            pst.setString(2, pass);
            pst.setInt(3, -1); // tài khoản mới chưa có quyền, chờ admin cấp
            pst.execute();
            JOptionPane.showMessageDialog(null, "Thêm tài khoản thành công");
            txtTenMoi.setText("");
            txtPassMoi.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Không thêm được (có thể trùng tên):\n" + e.getMessage());
        }
        updateTableTaiKhoan();
    }

    private void doiQuyen(int quyen) {
        int row = jTableTaiKhoan.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(null, "Hãy chọn 1 tài khoản trong bảng");
            return;
        }
        String ten = jTableTaiKhoan.getValueAt(row, 0).toString();
        try {
            // admin <> 2: không ai được đổi quyền của admin chính
            pst = conn.prepareStatement("update login set admin=? where username=? and admin<>2");
            pst.setInt(1, quyen);
            pst.setString(2, ten);
            int n = pst.executeUpdate();
            if (n == 0) {
                JOptionPane.showMessageDialog(null, "Không được đổi quyền của admin chính");
            } else {
                JOptionPane.showMessageDialog(null, "Đã cập nhật quyền cho " + ten);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        updateTableTaiKhoan();
    }
}
