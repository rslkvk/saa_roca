package dataaccess;

/**
 * Created by resul on 08/06/15.
 */
public class DAOFactory {

    private static ContactDAO contactDAO = null;

    private DAOFactory(){};

    public static ContactDAO getContactDAO() {
        if(contactDAO == null) {
            contactDAO = new CDBContactManagerDAO();
        }
        return contactDAO;
    }
}
