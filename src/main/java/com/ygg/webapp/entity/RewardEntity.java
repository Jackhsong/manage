package com.ygg.webapp.entity;

public class RewardEntity extends BaseEntity {
	
	 /**    */
	private static final long serialVersionUID = -3320803222370543433L;
	/**帐号ID    */
	private int accountId;
	 /**总奖励*/
	private float totalReward;
	  
	 /**已提现    */
	private float alreadyCash;
	  
	 /** 可提现   */
	private float withdrawCash;

	
	/**  
	 *@return  the accountId
	 */
	public int getAccountId() {
		return accountId;
	}

	
	/** 
	 * @param accountId the accountId to set
	 */
	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	
	/**  
	 *@return  the totalReward
	 */
	public float getTotalReward() {
		return totalReward;
	}

	
	/** 
	 * @param totalReward the totalReward to set
	 */
	public void setTotalReward(float totalReward) {
		this.totalReward = totalReward;
	}

	
	/**  
	 *@return  the alreadyCash
	 */
	public float getAlreadyCash() {
		return alreadyCash;
	}

	
	/** 
	 * @param alreadyCash the alreadyCash to set
	 */
	public void setAlreadyCash(float alreadyCash) {
		this.alreadyCash = alreadyCash;
	}

	
	/**  
	 *@return  the withdrawCash
	 */
	public float getWithdrawCash() {
		return withdrawCash;
	}

	
	/** 
	 * @param withdrawCash the withdrawCash to set
	 */
	public void setWithdrawCash(float withdrawCash) {
		this.withdrawCash = withdrawCash;
	}
}
