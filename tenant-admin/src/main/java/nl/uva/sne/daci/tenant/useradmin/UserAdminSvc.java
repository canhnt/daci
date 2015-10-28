/**
 * 
 */
package nl.uva.sne.daci.tenant.useradmin;

import java.util.List;

/**
 * @author canhnt
 *
 */
public interface UserAdminSvc {
	
	/**
	 * Add a user to the User Management
	 * @param userId
	 */
	public void addUser(String userId);
	
	public void deleteUser(String userId);
	
	public List<String> listUsers();
	
	/**
	 * Update/overwrite or add a new credential to a user in the User Management.
	 * 
	 * @param userId
	 * @param credential
	 */
	public void updateCredential(String userId, String credential);
	
	public void deleteCredential(String userId, String credentialId);
}
