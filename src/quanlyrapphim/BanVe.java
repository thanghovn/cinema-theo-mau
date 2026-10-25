package quanlyrapphim;

import java.sql.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class BanVe extends JInternalFrame {

    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;

    JTextField txtMaVe = new JTextField();
    JComboBox<String> cboMaSuat = new JComboBox<>();
    JTextField txtTenKhach = new JTextField();
    JTextField txtSoDienThoai = new JTextField();
    JTextField txtSoGhe = new JTextField();
    JTextField txtGiaVe = new JTextField();
    JLabel lbThongTinSuat = new JLabel();
    JLabel lbDoanhThu = new JLabel();
    JTextField txtTimKiem = new JTextField();
    JRadioButton jRadioButtonTenKhach = new JRadioButton("Tên khách", true);
    JRadioButton jRadioButtonSDT = new JRadioButton("Số điện thoại");
    JRadioButton jRadioButtonMaSuat = new JRadioButton("Mã suất");
    JButton btnThem = new JButton("Bán vé");
    JButton btnSua = new JButton("Sửa");
    JButton btnXoa = new JButton("Hủy vé");
    JButton btnReset = new JButton("Làm mới");
    JButton btnIn = new JButton("In");
    JTable jTableVe = new JTable();

    public BanVe() {
        initComponents();
        conn = JavaConnect.ConnectDB();
        updateTableVe();
        fillComboMaSuat();
        btnXoa.setVisible(Login.admin >= 1); // chỉ admin được hủy vé
    }

    private void initComponents() {
        setTitle("Bán vé");
        setBounds(0, 0, 960, 580);
        setLayout(null);

        String[] nhan = {"Mã vé:", "Mã suất:", "Tên khách:", "Số điện thoại:", "Số ghế:", "Giá vé (đồng):"};
        JComponent[] o = {txtMaVe, cboMaSuat, txtTenKhach, txtSoDienThoai, txtSoGhe, txtGiaVe};
        for (int i = 0; i < o.length; i++) {
            JLabel lb = new JLabel(nhan[i]);
            lb.setBounds(20, 20 + 38 * i, 120, 25);
            add(lb);
            o[i].setBounds(140, 20 + 38 * i, 250, 25);
            add(o[i]);
        }
        txtGiaVe.setEditable(false); // giá vé lấy tự động từ suất chiếu
        JLabel lbGoiY = new JLabel("(ví dụ A1)");
        lbGoiY.setBounds(395, 172, 100, 25);
        add(lbGoiY);
        lbThongTinSuat.setBounds(20, 228, 600, 25);
        add(lbThongTinSuat);

        btnThem.setBounds(500, 20, 80, 30);
        btnSua.setBounds(590, 20, 80, 30);
        btnXoa.setBounds(680, 20, 80, 30);
        btnReset.setBounds(770, 20, 90, 30);
        btnIn.setBounds(870, 20, 60, 30);
        add(btnThem);
        add(btnSua);
        add(btnXoa);
        add(btnReset);
        add(btnIn);

        JLabel lbTim = new JLabel("Tìm kiếm:");
        lbTim.setBounds(500, 80, 70, 25);
        add(lbTim);
        txtTimKiem.setBounds(570, 80, 360, 25);
        add(txtTimKiem);
        ButtonGroup nhom = new ButtonGroup();
        nhom.add(jRadioButtonTenKhach);
        nhom.add(jRadioButtonSDT);
        nhom.add(jRadioButtonMaSuat);
        jRadioButtonTenKhach.setBounds(570, 110, 100, 25);
        jRadioButtonSDT.setBounds(670, 110, 120, 25);
        jRadioButtonMaSuat.setBounds(790, 110, 90, 25);
        add(jRadioButtonTenKhach);
        add(jRadioButtonSDT);
        add(jRadioButtonMaSuat);

        lbDoanhThu.setFont(lbDoanhThu.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        lbDoanhThu.setBounds(570, 150, 360, 25);
        add(lbDoanhThu);

        JScrollPane scroll = new JScrollPane(jTableVe);
        scroll.setBounds(20, 260, 910, 270);
        add(scroll);

        btnThem.addActionListener(e -> btnThemActionPerformed());
        btnSua.addActionListener(e -> btnSuaActionPerformed());
        btnXoa.addActionListener(e -> btnXoaActionPerformed());
        btnReset.addActionListener(e -> btnResetActionPerformed());
        btnIn.addActionListener(e -> btnInActionPerformed());
        cboMaSuat.addActionListener(e -> hienThongTinSuat());
        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtTimKiemKeyReleased();
            }
        });
        jTableVe.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableVeMouseClicked();
            }
        });
    }

    private void updateTableVe() {
        try {
            pst = conn.prepareStatement("select * from TableVe");
            rs = pst.executeQuery();
            JavaConnect.loadTable(jTableVe, rs);
            tinhDoanhThu();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // Tổng tiền của tất cả vé đã bán
    private void tinhDoanhThu() throws SQLException {
        pst = conn.prepareStatement("select count(*), sum(GiaVe) from TableVe");
        rs = pst.executeQuery();
        if (rs.next()) {
            lbDoanhThu.setText("Đã bán: " + rs.getInt(1) + " vé  -  Doanh thu: " + rs.getLong(2) + " đồng");
        }
    }

    public void fillComboMaSuat() {
        cboMaSuat.removeAllItems();
        try {
            pst = conn.prepareStatement("select MaSuat from TableSuatChieu");
            rs = pst.executeQuery();
            while (rs.next()) {
                cboMaSuat.addItem(rs.getString("MaSuat"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // Chọn mã suất thì hiện tên phim, phòng, giờ và tự điền giá vé
    private void hienThongTinSuat() {
        if (cboMaSuat.getSelectedItem() == null) {
            lbThongTinSuat.setText("");
            txtGiaVe.setText("");
            return;
        }
        String sql = "select s.*, p.TenPhim from TableSuatChieu s join TablePhim p on s.MaPhim = p.MaPhim where s.MaSuat=?";
        try {
            PreparedStatement pst2 = conn.prepareStatement(sql);
            pst2.setString(1, cboMaSuat.getSelectedItem().toString());
            ResultSet rs2 = pst2.executeQuery();
            if (rs2.next()) {
                lbThongTinSuat.setText("Phim: " + rs2.getString("TenPhim") + "  |  " + rs2.getString("PhongChieu")
                        + "  |  " + rs2.getString("GioChieu") + " ngày " + rs2.getString("NgayChieu"));
                txtGiaVe.setText(rs2.getString("GiaVe"));
            }
            rs2.close();
            pst2.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private boolean kiemTra() {
        if (txtMaVe.getText().trim().isEmpty() || cboMaSuat.getSelectedItem() == null
                || txtTenKhach.getText().trim().isEmpty() || txtSoGhe.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Phải nhập mã vé, tên khách, số ghế và chọn suất chiếu");
            return false;
        }
        String sdt = txtSoDienThoai.getText().trim();
        if (!sdt.isEmpty() && !sdt.matches("0\\d{9}")) {
            JOptionPane.showMessageDialog(null, "Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 0");
            return false;
        }
        return true;
    }

    // Một ghế trong một suất chỉ bán cho một vé
    private boolean gheDaBan() throws SQLException {
        String sql = "select * from TableVe where MaSuat=? and SoGhe=? and MaVe<>?";
        pst = conn.prepareStatement(sql);
        pst.setString(1, cboMaSuat.getSelectedItem().toString());
        pst.setString(2, txtSoGhe.getText().trim().toUpperCase());
        pst.setString(3, txtMaVe.getText().trim());
        rs = pst.executeQuery();
        return rs.next();
    }

    private void btnThemActionPerformed() {
        if (!kiemTra()) {
            return;
        }
        try {
            if (gheDaBan()) {
                JOptionPane.showMessageDialog(null, "Ghế này đã có người mua trong suất chiếu này");
                return;
            }
            String sql = "Insert into TableVe(MaVe,MaSuat,TenKhach,SoDienThoai,SoGhe,GiaVe,NgayBan) values (?,?,?,?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, txtMaVe.getText().trim());
            pst.setString(2, cboMaSuat.getSelectedItem().toString());
            pst.setString(3, txtTenKhach.getText().trim());
            pst.setString(4, txtSoDienThoai.getText().trim());
            pst.setString(5, txtSoGhe.getText().trim().toUpperCase());
            pst.setInt(6, Integer.parseInt(txtGiaVe.getText()));
            pst.setString(7, new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            pst.execute();
            JOptionPane.showMessageDialog(null, "Bán vé thành công");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Không bán được (có thể trùng mã vé):\n" + e.getMessage());
        }
        updateTableVe();
    }

    private void btnSuaActionPerformed() {
        if (!kiemTra()) {
            return;
        }
        try {
            if (gheDaBan()) {
                JOptionPane.showMessageDialog(null, "Ghế này đã có người mua trong suất chiếu này");
                return;
            }
            String sql = "update TableVe set MaSuat=?,TenKhach=?,SoDienThoai=?,SoGhe=?,GiaVe=? where MaVe=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, cboMaSuat.getSelectedItem().toString());
            pst.setString(2, txtTenKhach.getText().trim());
            pst.setString(3, txtSoDienThoai.getText().trim());
            pst.setString(4, txtSoGhe.getText().trim().toUpperCase());
            pst.setInt(5, Integer.parseInt(txtGiaVe.getText()));
            pst.setString(6, txtMaVe.getText().trim());
            int n = pst.executeUpdate();
            if (n == 0) {
                JOptionPane.showMessageDialog(null, "Không tìm thấy mã vé để sửa");
            } else {
                JOptionPane.showMessageDialog(null, "Sửa thành công");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        updateTableVe();
    }

    private void btnXoaActionPerformed() {
        int p = JOptionPane.showConfirmDialog(null, "Bạn chắc chắn muốn hủy vé này", "Hủy vé", JOptionPane.YES_NO_OPTION);
        if (p == 0) {
            try {
                pst = conn.prepareStatement("delete from TableVe where MaVe =?");
                pst.setString(1, txtMaVe.getText().trim());
                pst.execute();
                JOptionPane.showMessageDialog(null, "Hủy vé thành công");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
            }
            btnResetActionPerformed();
        }
    }

    private void btnResetActionPerformed() {
        txtMaVe.setText("");
        txtTenKhach.setText("");
        txtSoDienThoai.setText("");
        txtSoGhe.setText("");
        txtTimKiem.setText("");
        updateTableVe();
    }

    private void btnInActionPerformed() {
        MessageFormat header = new MessageFormat("Danh sách vé đã bán");
        MessageFormat footer = new MessageFormat("Trang {0,number,integer}");
        try {
            jTableVe.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Không thể in: " + e.getMessage());
        }
    }

    private void jTableVeMouseClicked() {
        int row = jTableVe.getSelectedRow();
        if (row < 0) {
            return;
        }
        txtMaVe.setText(jTableVe.getValueAt(row, 0).toString());
        cboMaSuat.setSelectedItem(jTableVe.getValueAt(row, 1).toString());
        txtTenKhach.setText(jTableVe.getValueAt(row, 2).toString());
        txtSoDienThoai.setText(jTableVe.getValueAt(row, 3).toString());
        txtSoGhe.setText(jTableVe.getValueAt(row, 4).toString());
        txtGiaVe.setText(jTableVe.getValueAt(row, 5).toString());
    }

    private void txtTimKiemKeyReleased() {
        String sql = "select * from TableVe where TenKhach like ?";
        if (jRadioButtonSDT.isSelected()) {
            sql = "select * from TableVe where SoDienThoai like ?";
        }
        if (jRadioButtonMaSuat.isSelected()) {
            sql = "select * from TableVe where MaSuat like ?";
        }
        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + txtTimKiem.getText().trim() + "%");
            rs = pst.executeQuery();
            JavaConnect.loadTable(jTableVe, rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
}
