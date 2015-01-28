/**
 * This interface describes the Transaction object. The interface
 * provides for dependency injection and easy mock for testing.
 */

package co.tapdatapp.tapandroid.localdata;

public interface TransactionDAO {
  /**
   * Total number of Transaction records
   *
   * @return number of transaction records
   */
  public int getRecordCount();

  /**
   * Return an object for transaction #x as requested
   *
   * @param itemLocation the location ordered reverse-chronologically
   * @return TransactionDAO object matching the requested
   */
  public TransactionDAO getByOrder(int itemLocation);

  /**
   * Load this object with the data from transaction #x
   *
   * @param location the transaction # to move to
   */
  public void moveTo(int location);
}
