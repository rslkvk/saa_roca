package dataaccess;

import logic.exception.DBException;
import logic.exception.FilterException;

import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Created by resul on 08/06/15.
 */
public interface ContactDAO {
    /**
     * This method creates new contact in the database
     * @param contact as JSON String
     * @return Response object
     * @throws DBException
     * @throws IOException
     */
    public Response createContact(String contact) throws DBException, IOException;


    /**
     *
     * @param contact the contact identifier
     * @return Response Object
     * @throws DBException if a failure occurred by the database
     */
    public Response readContact(String contact) throws DBException, IOException;

    /**
     *
     * @param contactId the URI from the contact
     * @param content the JSON content from contact form
     * @return Response Object
     * @throws DBException
     */
    public Response updateContact(String contactId, String content, String revision) throws DBException;

    /**
     * This method removes a contact from the database
     * @param contact the contact identifier
     * @return Response object
     * @throws DBException
     */
    public Response deleteContact(String contact, String revision) throws DBException;

    /**
     * get contact list
     * @return
     */
    public Response getContacts() throws DBException, FilterException, IOException;

    public Response searchContacts(String search) throws DBException;
    public Response filterContacts(String filter) throws DBException, FilterException, IOException;
    public Response sortContacts(String sort) throws DBException, IOException;


    /**
     * contacts can be null, if no contact added.
     * @param group
     * @param contacts
     * @return
     */
    public Response createGroup(String group, String contacts) throws DBException;
    public Response readGroup(String name) throws DBException;
    public Response updateGroup(String group) throws DBException;
    public Response deleteGroup(String name) throws DBException;

    public Response addContact(String groupName, String contact) throws DBException;
    public Response getGroups() throws DBException;


    public Response setFavoriteById(String contactId) throws DBException, IOException;
    public Response setFavorite(String contact) throws DBException, IOException;
    Response deleteFavorite(String contactId) throws DBException;
    public Response getFavorites() throws DBException, IOException;
}
