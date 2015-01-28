/**
 * Layer over the SQLite database that provides dynamic access to
 * transaction history as required by the history ListView
 */

package co.tapdatapp.tapandroid.history;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.TransactionDAO;

public class HistoryAdapter implements ListAdapter {

  private DataSetObserver dso;
  private Integer recordCount = null;
  private TransactionDAO dao;

  public HistoryAdapter(TransactionDAO t) {
    dao = t;
  }

  @Override
  public boolean areAllItemsEnabled() {
    return false;
  }

  @Override
  public boolean isEnabled(int i) {
    return false;
  }

  @Override
  public void registerDataSetObserver(DataSetObserver dataSetObserver) {
    if (dso != null) {
      throw new AssertionError("Attempt to set DataSetObserver 2x");
    }
    dso = dataSetObserver;
  }

  @Override
  public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
    if (dso != dataSetObserver) {
      throw new AssertionError("Attempt to unregister non-registered DSO");
    }
    dso = null;
  }

  @Override
  public int getCount() {
    setCount();
    return recordCount;
  }

  @Override
  public Object getItem(int i) {
    return dao.getByOrder(i);
  }

  @Override
  public long getItemId(int i) {
    return i;
  }

  @Override
  public boolean hasStableIds() {
    return false;
  }

  @Override
  public View getView(int i, View view, ViewGroup viewGroup) {
    return null;
  }

  @Override
  public int getItemViewType(int i) {
    return R.layout.history_line_item;
  }

  @Override
  public int getViewTypeCount() {
    return 1;
  }

  @Override
  public boolean isEmpty() {
    setCount();
    return recordCount == 0;
  }

  private void setCount() {
    if (recordCount == null) {
      recordCount = dao.getRecordCount();
    }
  }
}
