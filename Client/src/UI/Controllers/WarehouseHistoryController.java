package UI.Controllers;

import UI.Models.DomainModels.BookItemModel;
import UI.Models.DomainModels.BookModel;
import UI.Models.DomainModels.StaffModel;
import UI.Models.DomainModels.WarehouseHistoryModel;
import UI.Models.TableViewItemModel.WarehouseHistoryRowItem;
import UI.Views.BaseScene;
import UIComponents.TableView.TableViewDelegate;
import com.java.project.InfoEntry;
import com.java.project.Utils;
import data.Repositories.BookItemRepository;
import data.Repositories.WarehouseHistoryRepository;
import utils.DB.TransformException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

public class WarehouseHistoryController extends BaseController implements TableViewDelegate<WarehouseHistoryRowItem> {

    private final WarehouseHistoryRepository repository = new WarehouseHistoryRepository();
    private final BookItemRepository bookItemRepository = new BookItemRepository();
    private final ArrayList<WarehouseHistoryRowItem> rowItems = new ArrayList();
    private HashSet<WarehouseHistoryRowItem> selectedObjects = new HashSet<>();

    public WarehouseHistoryController(BaseScene scene) {
        super(scene);
        this.scene.setTableViewDelegate(this);
    }


    @Override
    public void onAppear() {
        this.reloadData();
    }

    @Override
    public void onDisappear() {
        // Empty
    }

    @Override
    public void reloadData() {
        this.rowItems.clear();
        try {
            this.rowItems.addAll(this.repository
                    .getAll()
                    .stream()
                    .map(WarehouseHistoryRowItem::new)
                    .collect(Collectors.toList()));
        } catch (TransformException | SQLException e) {
            e.printStackTrace();
            Utils.showError();
        }

        if (rowItems.size() == 0) {
            var item = new WarehouseHistoryRowItem();
            rowItems.add(item);
        }

        this.scene.reloadTableData();
    }


    @Override
    void onCreateTapped() {
        InfoEntry[] infos = {
                new InfoEntry("STAFF ID", StaffModel.class),
                new InfoEntry("BOOK ID", BookModel.class),
                new InfoEntry("AMOUNT", Integer.class),
        };
        var arr = new ArrayList<InfoEntry>(Arrays.asList(infos));

        Utils.createPopup(arr, res -> {
            // var item = new WarehouseHistoryModel();
            // item.setStaffId((Integer) res.get(0));
            // item.setBookItemId((Integer)res.get(1));
            var bookId = (Integer)res.get(1);
            var amount = (Integer)res.get(2);
            for (var i = 0; i < amount; i++) {
                var b = new BookItemModel();
                b.setBook_id(bookId);
                try {
                    b = this.bookItemRepository.create(b);
                    var w = new WarehouseHistoryModel();
                    w.setStaffId((Integer) res.get(0));
                    w.setBookItemId(b.getId());
                    this.repository.create(w);
                } catch (TransformException | SQLException e) {
                    e.printStackTrace();
                    Utils.showError(e.getMessage());
                }
            }
        });

        reloadData();
    }

    @Override
    void onUpdatedTapped() {
        if (this.selectedObjects.size() <= 0) return;
        var o = this.selectedObjects.stream().findFirst().get().getModel();

        InfoEntry[] infos = {
                new InfoEntry("STAFF ID", StaffModel.class, o.getStaffId()),
                new InfoEntry("BOOK ITEM ID", BookItemModel.class, o.getBookItemId()),
                new InfoEntry("CREATED AT", o.getCreatedAt()),
        };
        var arr = new ArrayList<InfoEntry>(Arrays.asList(infos));

        Utils.updatePopup(arr, res -> {
            o.setStaffId((Integer) res.get(0));
            o.setBookItemId((Integer)res.get(1));
            o.setCreatedAt((Date)res.get(2));
            try {
                this.repository.update(o);
            } catch (TransformException | SQLException e) {
                e.printStackTrace();
                Utils.showError();
            }
        });

        reloadData();
    }

    @Override
    void onDeleteTapped() {
        for (var o: this.selectedObjects) {
            var id = o.getId();
            try {
                this.repository.delete(id);
            } catch (SQLException throwables) {
                Utils.showError(throwables.getMessage());
            }
        }
        this.reloadData();
    }


    //
    // Implement table delegates
    //
    @Override
    public int getRowCount() {
        return this.rowItems.size();
    }

    @Override
    public Class<?> rowItemClass() {
        return WarehouseHistoryRowItem.class;
    }

    @Override
    public WarehouseHistoryRowItem itemAt(int row) {
        return this.rowItems.get(row);
    }

    @Override
    public void tableViewDidSelectRow(int[] rows) {
        if (this.rowItems.size() > 0 && rows.length > 0) {
            this.selectedObjects.clear();
            for (var i: rows) {
                this.selectedObjects.add(this.rowItems.get(i));
            }
        }
    }

    @Override
    void onSearchButtonTapped(String searchText) {
        this.rowItems.clear();
        try {
            this.rowItems.addAll(this.repository
                    .searchName(searchText)
                    .stream()
                    .map(WarehouseHistoryRowItem::new)
                    .collect(Collectors.toList()));
        } catch (TransformException | SQLException e) {
            e.printStackTrace();
            Utils.showError();
        }

        if (rowItems.size() == 0) {
            var item = new WarehouseHistoryRowItem();
            rowItems.add(item);
        }

        this.scene.reloadTableData();
    }

    @Override
    void onClearButtonTapped() {
        this.reloadData();
        super.onClearButtonTapped();
    }
}