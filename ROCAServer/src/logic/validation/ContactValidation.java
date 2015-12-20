package logic.validation;

import logic.exception.ContactException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by resul on 31/05/15.
 */
public class ContactValidation {

    public static void validateContact(Map<String, String> contact) throws ContactException {

        Set<String> keys = contact.keySet();
        Iterator<String> iter = keys.iterator();

        while(iter.hasNext()) {
            String s = iter.next();
            switch (s) {
                case "first_name":
                    String fname = contact.get(s);
                    ContactValidation.validateFirstName(fname);
                    break;
                case "last_name":
                    String lname = contact.get(s);
                    ContactValidation.validateLastName(lname);
                    break;
                case "company_name":
                    String cname = contact.get(s);
                    ContactValidation.validateCompanyName(cname);
                    break;
                case "address":
                    String address = contact.get(s);
                    ContactValidation.validateAddress(address);
                    break;
                case "city":
                    String city = contact.get(s);
                    ContactValidation.validateCity(city);
                    break;
                case "country":
                    String country = contact.get(s);
                    ContactValidation.validateCity(country);
                    break;
                case "postal":
                    String postal = contact.get(s);
                    ContactValidation.validatePostal(postal);
                    break;
                case "phone1":
                    String phone = contact.get(s);
                    ContactValidation.validatePhone(phone);
                    break;
                case "phone2":
                    String phoneopt = contact.get(s);
                    ContactValidation.validatePhoneOpt(phoneopt);
                    break;
                case "email":
                    String email = contact.get(s);
                    ContactValidation.validateEMail(email);
                    break;
                case "web":
                    String web = contact.get(s);
                    ContactValidation.validateWebsite(web);
                    break;
                case "groups":
                    break;
                case "favorite":
                    break;
                case "type":
                    break;
                case "_rev":
                    break;
                case "_method":
                    break;
                case "_id":
                    System.out.println("ID**");
                    break;

                default:
                    throw new ContactException("The contact formular is incorrect. Please inform the webmaster or admin.");
            }

        }

    }

    public static boolean validateFirstName(String firstName) throws ContactException {
        Pattern p = Pattern.compile("([a-zA-z]+(\\s?))+");
        if(p.matcher(firstName).matches()) {
            return true;
        }
        throw new ContactException("Your first name is empty or invalid. " +
                "The required field should include your first name and may not contain invalid characters (ie. $, <, >, /, etc.). " +
                "Please type your first name or check your input.");
    }

    public static boolean validateLastName(String lastName) throws ContactException {
        Pattern p = Pattern.compile("([a-zA-z]+(\\s?))+");
        if(p.matcher(lastName).matches()) {
            return true;
        }
        throw new ContactException("Your last name is empty or invalid. " +
                "The required field should include your last name and may not contain invalid characters (ie. $, <, >, /, etc.). " +
                "Please type your last name or check your input.");
    }

    public static boolean validateCompanyName(String companyName) throws ContactException {
        Pattern p = Pattern.compile("((\\w+)(\\W*)(\\s?))+");
        if(p.matcher(companyName).matches()) {
            return true;
        }
        throw new ContactException("Your company name is empty or invalid. " +
                "The required field should include your company name (begins with A-Z or 0-9) and may not be empty. " +
                "Please type your company name or check your input.");
    }

    public static boolean validateAddress(String address) throws ContactException{
        /* german streeet pattern
        Pattern p = Pattern.compile("((([a-zA-Z]{3,})(\\.?|\\:?)(\\s?))+([1-9]+[a-zA-Z]?))");
        */
        Pattern p = Pattern.compile("((\\w+)(\\W*)(\\s?))+");
        if(p.matcher(address).matches()) {
            return true;
        }
        throw new ContactException("Your address is empty or invalid. " +
                "The required field should include your address (begins with A-Z or 0-9) and may not be empty. " +
                "Please type your company name or check your input.");
    }

    public static boolean validateCity(String city) throws ContactException {
        Pattern p = Pattern.compile("(([a-zA-Z])(\\.?)(\\s?))+");
        if(p.matcher(city).matches()) {
            return true;
        }
        throw new ContactException("Your city is empty or invalid. " +
                "The required field should include your city and may not contain invalid characters (ie. $, <, >, /, etc.). " +
                "Please type your city or check your input.");
    }

    public static boolean validatePostal(String postal) throws ContactException {
        Pattern p = Pattern.compile("(([\\w]{2,})(\\s?))+");
        if(p.matcher(postal).matches()) {
            return true;
        }
        throw new ContactException("Your postal code is empty or invalid. " +
                "The required field should include your postal code and may contain only numbers (0-9). " +
                "Please type your postal code or check your input.");
    }

    public static boolean validatePhone(String phoneNumber) throws ContactException {
        Pattern p = Pattern.compile("([0-9]+)(\\-?)([0-9]+)");
        if(p.matcher(phoneNumber).matches()) {
            return true;
        }
        throw new ContactException("Your first phone number is empty or invalid. " +
                "The required field should include your phone number and may contain only numbers or \"-\" (ie. 421313, 1234-54321). " +
                "Please type your phone number or check your input.");
    }

    public static boolean validatePhoneOpt(String phoneNumber) throws ContactException {
        Pattern p = Pattern.compile("(([0-9]+)(\\-?)([0-9]+))?");
        if(p.matcher(phoneNumber).matches()) {
            return true;
        }
        throw new ContactException("Your second phone number is invalid. " +
                "The optional field should include your phone number and may contain only numbers or \"-\" (ie. 421313, 1234-54321). " +
                "Please check your phone number.");
    }

    public static boolean validateEMail(String email) throws ContactException {
        Pattern p = Pattern.compile("(((\\w+)(\\.?|\\-?))+(\\@{1})(((\\w+)(\\.?|\\-?))+(\\.[a-z]{2,})+))?");
        if(p.matcher(email).matches()) {
            return true;
        }
        throw new ContactException("Your email address is invalid. " +
                "The optional field should include your email address (ie. user@test.com)" +
                "Please check your email address.");
    }

    public static boolean validateWebsite(String website) throws ContactException {
        Pattern p = Pattern.compile("(((http://)?(www)?(((\\w+)(\\.?|\\-?))+(\\.[a-z]{2,})+))|\\s)?");
        if(p.matcher(website).matches()) {
            return true;
        }
        throw new ContactException("Your website is invalid. " +
                "The optional field should include your website (ie. http://www.test.com, www.test.com, test.com ...)" +
                "Please check your website.");
    }

}
