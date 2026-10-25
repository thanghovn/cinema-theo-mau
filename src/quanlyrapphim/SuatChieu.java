package quanlyrapphim;

import java.sql.*;
import java.text.MessageFormat;
import javax.swing.*;

public class SuatChieu extends JInternalFrame {

    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;

    JTextField txtMaSuat = new JTextField();
    JComboBox<String> cboMaPhim = new JComboBox<>();
    JComboBox<String> cboPhongChieu = new JComboBox<>(new String[]{"Phòng 1", "Phòng 2", "Phòng 3"});
    JTextField txtNgayChieu = new JTextField();
    JTextField txtGioChieu = new JTextField();
    JTextField txtGiaVe = new JTextField();
    JTextField txtTimKiem = new JTextField();
    JRadioButton jRadioButtonMaPhim = new JRadioButton("Mã phim", true);
    JRadioButton jRadioButtonNgayChieu = new JRadioButton("Ngày chiếu");
    JComboBox<String> jComboBoxSapXep = new JComboBox<>(new String[]{"Sắp xếp theo mã", "Sắp xếp theo ngày chiếu", "Sắp xếp theo giá vé"});
    JButton btnThem = new JButton("Thêm");
    JButton btnSua = new JButton("Sửa");
    JButton btnXoa = new JButton("Xóa");
    JButton btnReset = new JButton("Làm mới");
    JButton btnIn = new JButton("In");
    JTable jTableSuatChieu = new JTable();

    public SuatChieu() {
        initComponents();
        conn = JavaConnect.ConnectDB();
        updateTableSuatChieu();
        fillComboMaPhim();
        // Chỉ admin được thêm/sửa/xóa suất chiếu
        btnThem.setVisible(Login.admin >= 1);
        btnSua.setVisible(Login.admin >= 1);
        btnXoa.setVisible(Login.admin >= 1);
    }

    private void initComponents() {
        setTitle("Quản lý suất chiếu");
        setBounds(0, 0, 960, 580);
        setLayout(null);

        String[] nhan = {"Mã suất:", "Mã phim:", "Phòng chiếu:", "Ngày chiếu:", "Giờ chiếu:", "Giá vé (đồng):"};
        JComponent[] o = {txtMaSuat, cboMaPhim, cboPhongChieu, txtNgayChieu, txtGioChieu, txtGiaVe};
        for (int i = 0; i < o.length; i++) {
            JLabel lb = new JLabel(nhan[i]);
            lb.setBounds(20, 20 + 38 * i, 120, 25);
            add(lb);
            o[i].setBounds(140, 20 + 38 * i, 250, 25);
            add(o[i]);
        }
        JLabel lbGoiY1 = new JLabel("(dd/MM/yyyy)");
        lbGoiY1.setBounds(395, 134, 100, 25);
        add(lbGoiY1);
        JLabel lbGoiY2 = new JLabel("(HH:mm)");
        lbGoiY2.setBounds(395, 172, 100, 25);
        add(lbGoiY2);

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
        nhom.add(jRadioButtonMaPhim);
        nhom.add(jRadioButtonNgayChieu);
        jRadioButtonMaPhim.setBounds(570, 110, 90, 25);
        jRadioButtonNgayChieu.setBounds(660, 110, 110, 25);
        add(jRadioButtonMaPhim);
        add(jRadioButtonNgayChieu);

        jComboBoxSapXep.setBounds(570, 150, 200, 25);
        add(jComboBoxSapXep);

        JScrollPane scroll = new JScrollPane(jTableSuatChieu);
        scroll.setBounds(20, 260, 910, 270);
        add(scroll);

        btnThem.addActionListener(e -> btnThemActionPerformed());
        btnSua.addActionListener(e -> btnSuaActionPerformed());
        btnXoa.addActionListener(e -> btnXoaActionPerformed());
        btnReset.addActionListener(e -> btnResetActionPerformed());
        btnIn.addActionListener(e -> btnInActionPerformed());
        jComboBoxSapXep.addActionListener(e -> jComboBoxSapXepItemStateChanged());
        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtTimKiemKeyReleased();
            }
        });
        jTableSuatChieu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTableSuatChieuMouseClicked();
            }
        });
    }

    private void updateTableSuatChieu() {
        String sql = "select * from TableSuatChieu";
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            JavaConnect.loadTable(jTableSuatChieu, rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // Lấy danh sách mã phim đổ vào combo, giống fillComboMaThuoc() của bài mẫu
    public void fillComboMaPhim() {
        cboMaPhim.removeAllItems();
        try {
            pst = conn.prepareStatement("select MaPhim from TablePhim");
            rs = pst.executeQuery();
            while (rs.next()) {
                cboMaPhim.addItem(rs.getString("MaPhim"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private boolean laGio(String s) {
        if (!s.matches("\\d{2}:\\d{2}")) {
            return false;
        }
        int gio = Integer.parseInt(s.substring(0, 2));
        int phut = Integer.parseInt(s.substring(3, 5));
        return gio < 24 && phut < 60;
    }

    private boolean kiemTra() {
        if (txtMaSuat.getText().trim().isEmpty() || cboMaPhim.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(null, "Phải nhập mã suất và chọn mã phim");
            return false;
        }
        if (!JavaConnect.laNgay(txtNgayChieu.getText().trim())) {
            JOptionPane.showMessageDialog(null, "Ngày chiếu phải có dạng dd/MM/yyyy");
            return false;
        }
        if (!laGio(txtGioChieu.getText().trim())) {
            JOptionPane.showMessageDialog(null, "Giờ chiếu phải có dạng HH:mm, ví dụ 19:30");
            return false;
        }
        try {
            if (Integer.parseInt(txtGiaVe.getText().trim()) <= 0) {
                JOptionPane.showMessageDialog(null, "Giá vé phải lớn hơn 0");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Giá vé phải là số nguyên");
            return false;
        }
        return true;
    }

    // Cùng phòng, cùng ngày, cùng giờ mà khác mã suất là trùng lịch
    private boolean trungLich() throws SQLException {
        String sql = "select * from TableSuatChieu where PhongChieu=? and NgayChieu=? and GioChieu=? and MaSuat<>?";
        pst = conn.prepareStatement(sql);
        pst.setString(1, cboPhongChieu.getSelectedItem().toString());
        pst.setString(2, txtNgayChieu.getText().trim());
        pst.setString(3, txtGioChieu.getText().trim());
        pst.setString(4, txtMaSuat.getText().trim());
        rs = pst.executeQuery();
        return rs.next();
    }

    private void btnThemActionPerformed() {
        if (!kiemTra()) {
            return;
        }
        try {
            if (trungLich()) {
                JOptionPane.showMessageDialog(null, "Phòng này đã có suất chiếu vào giờ đó");
                return;
            }
            String sql = "Insert into TableSuatChieu(MaSuat,MaPhim,PhongChieu,NgayChieu,GioChieu,GiaVe) values (?,?,?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, txtMaSuat.getText().trim());
            pst.setString(2, cboMaPhim.getSelectedItem().toString());
            pst.setString(3, cboPhongChieu.getSelectedItem().toString());
            pst.setString(4, txtNgayChieu.getText().trim());
            pst.setString(5, txtGioChieu.getText().trim());
            pst.setInt(6, Integer.parseInt(txtGiaVe.getText().trim()));
            pst.execute();
            JOptionPane.showMessageDialog(null, "Lưu thành công");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Không thêm được (có thể trùng mã suất):\n" + e.getMessage());
        }
        updateTableSuatChieu();
    }

    private void btnSuaActionPerformed() {
        if (!kiemTra()) {
            return;
        }
        try {
            if (trungLich()) {
                JOptionPane.showMessageDialog(null, "Phòng này đã có suất chiếu vào giờ đó");
                return;
            }
            String sql = "update TableSuatChieu set MaPhim=?,PhongChieu=?,NgayChieu=?,GioChieu=?,GiaVe=? where MaSuat=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, cboMaPhim.getSelectedItem().toString());
            pst.setString(2, cboPhongChieu.getSelectedItem().toString());
            pst.setString(3, txtNgayChieu.getText().trim());
            pst.setString(4, txtGioChieu.getText().trim());
            pst.setInt(5, Integer.parseInt(txtGiaVe.getText().trim()));
            pst.setString(6, txtMaSuat.getText().trim());
            int n = pst.executeUpdate();
            if (n == 0) {
                JOptionPane.showMessageDialog(null, "Không tìm thấy mã suất để sửa");
            } else {
                JOptionPane.showMessageDialog(null, "Sửa thành công");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        updateTableSuatChieu();
    }

    private void btnXoaActionPerformed() {
        int p = JOptionPane.showConfirmDialog(null, "Bạn chắc chắn muốn xóa", "Xóa", JOptionPane.YES_NO_OPTION);
        if (p == 0) {
            try {
                // Suất đã bán vé thì không cho xóa
                pst = conn.prepareStatement("select * from TableVe where MaSuat=?");
                pst.setString(1, txtMaSuat.getText().trim());
                rs = pst.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(null, "Suất này đã bán vé, không thể xóa");
                    return;
                }
                pst = conn.prepareStatement("delete from TableSuatChieu where MaSuat =?");
                pst.setString(1, txtMaSuat.getText().trim());
                pst.execute();
                JOptionPane.showMessageDialog(null, "Xóa thành công");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
            }
            updateTableSuatChieu();
            btnResetActionPerformed();
        }
    }

    private void btnResetActionPerformed() {
        txtMaSuat.setText("");
        txtNgayChieu.setText("");
        txtGioChieu.setText("");
        txtGiaVe.setText("");
        txtTimKiem.setText("");
        updateTableSuatChieu();
    }

    private void btnInActionPerformed() {
        MessageFormat header = new MessageFormat("Danh sách suất chiếu");
        MessageFormat footer = new MessageFormat("Trang {0,number,integer}");
        try {
            jTableSuatChieu.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Không thể in: " + e.getMessage());
        }
    }

    private void jTableSuatChieuMouseClicked() {
        int row = jTableSuatChieu.getSelectedRow();
        if (row < 0) {
            return;
        }
        txtMaSuat.setText(jTableSuatChieu.getValueAt(row, 0).toString());
        cboMaPhim.setSelectedItem(jTableSuatChieu.getValueAt(row, 1).toString());
        cboPhongChieu.setSelectedItem(jTableSuatChieu.getValueAt(row, 2).toString());
        txtNgayChieu.setText(jTableSuatChieu.getValueAt(row, 3).toString());
        txtGioChieu.setText(jTableSuatChieu.getValueAt(row, 4).toString());
        txtGiaVe.setText(jTableSuatChieu.getValueAt(row, 5).toString());
    }

    private void txtTimKiemKeyReleased() {
        String sql = "select * from TableSuatChieu where MaPhim like ?";
        if (jRadioButtonNgayChieu.isSelected()) {
            sql = "select * from TableSuatChieu where NgayChieu like ?";
        }
        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + txtTimKiem.getText().trim() + "%");
            rs = pst.executeQuery();
            JavaConnect.loadTable(jTableSuatChieu, rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void jComboBoxSapXepItemStateChanged() {
        String sql = "select * from TableSuatChieu order by MaSuat";
        int index = jComboBoxSapXep.getSelectedIndex();
        if (index == 1) {
            // Ngày lưu dạng dd/MM/yyyy nên phải cắt chuỗi ra năm-tháng-ngày rồi mới sắp xếp đúng
            sql = "select * from TableSuatChieu order by substr(NgayChieu,7,4), substr(NgayChieu,4,2), substr(NgayChieu,1,2), GioChieu";
        }
        if (index == 2) {
            sql = "select * from TableSuatChieu order by GiaVe";
        }
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            JavaConnect.loadTable(jTableSuatChieu, rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
}
