package UI.Controllers;

import UI.Models.AuthorModel;
import UI.Models.TableViewItemModel.AuthorRowItem;
import UI.Views.BaseScene;
import UIComponents.TableView.TableViewDelegate;
import com.java.project.InfoEntry;
import com.java.project.Utils;
import data.Repositories.AuthorRepository;
import utils.DB.TransformException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

public class AuthorController extends BaseController implements TableViewDelegate<AuthorRowItem> {

    private final AuthorRepository repository = new AuthorRepository();
    private final ArrayList<AuthorRowItem> rowItems = new ArrayList();
    private HashSet<AuthorRowItem> selectedObjects = new HashSet<>();

    public AuthorController(BaseScene scene) {
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
                    .map(AuthorRowItem::new)
                    .collect(Collectors.toList()));
        } catch (TransformException | SQLException e) {
            e.printStackTrace();
            Utils.showError();
        }

        if (rowItems.size() == 0) {
            var item = new AuthorRowItem();
            rowItems.add(item);
        }

        this.scene.reloadTableData();
    }


    @Override
    void onCreateTapped() {
        InfoEntry[] infos = {
                new InfoEntry("AUTHOR NAME", String.class),
        };
        var arr = new ArrayList<InfoEntry>(Arrays.asList(infos));

        Utils.createPopup(arr, res -> {
            var item = new AuthorModel();
            item.setName((String)res.get(0));
            try {
                this.repository.create(item);
            } catch (TransformException | SQLException e) {
                e.printStackTrace();
                Utils.showError();
            }
        });

        reloadData();
    }

    @Override
    void onUpdatedTapped() {
        if (this.selectedObjects.size() <= 0) return;
        var o = this.selectedObjects.stream().findFirst().get().getModel();

        InfoEntry[] infos = {
                new InfoEntry("AUTHOR NAME", o.getName()),
        };
        var arr = new ArrayList<InfoEntry>(Arrays.asList(infos));

        Utils.updatePopup(arr, res -> {
            var item = new AuthorModel();
            item.setName((String)res.get(0));
            try {
                this.repository.update(item);
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
                Utils.showError();
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
        return AuthorRowItem.class;
    }

    @Override
    public AuthorRowItem itemAt(int row) {
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
                    .map(AuthorRowItem::new)
                    .collect(Collectors.toList()));
        } catch (TransformException | SQLException e) {
            e.printStackTrace();
            Utils.showError();
        }

        if (rowItems.size() == 0) {
            var item = new AuthorRowItem();
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