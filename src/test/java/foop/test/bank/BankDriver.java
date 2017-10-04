/**
 * Project: Foops_1
 * Package: foop.test.bank
 * File: BankDriver.java
 * 
 * @author sidmishraw
 *         Last modified: Oct 3, 2017 4:35:05 PM
 */
package foop.test.bank;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import foop.core.StateManager;
import foop.core.Transaction;
import foop.utils.InstanceFactory;
import foop.utils.Transactions;

/**
 * @author sidmishraw
 *
 *         Qualified Name: foop.test.bank.BankDriver
 *
 */
public class BankDriver {
    
    private static final Logger logger  = LoggerFactory.getLogger(BankDriver.class);
    
    private static StateManager manager = InstanceFactory.getInstance(StateManager.class);
    private static Transactions ts      = InstanceFactory.getInstance(Transactions.class);
    
    /**
     * <p>
     * A probable method for taking care of bank account operations
     * Deposits the amount into the bank account
     * 
     * @param account
     *            The bank account name
     * @param amount
     *            The amount to be deposited
     */
    private static void deposit(String account, float amount) {
        
        logger.info(String.format("Depositing amount:: %f into bank account:: %s", amount, account));
        
        AccountBalance b = manager.read(account).isPresent() ? (AccountBalance) manager.read(account).get()
                : new AccountBalance(0);
        
        AccountBalance bnew = new AccountBalance(b.getBalance() + amount);
        
        manager.write(account, bnew);
        
        logger.info(String.format("Deposited amount:: %f into bank account:: %s, new amount:: %f", amount, account,
                ((AccountBalance) manager.read(account).get()).getBalance()));
    }
    
    /**
     * <p>
     * Another probable method for taking care of bank account operations
     * Withdraws the specified amount from bank accounts
     * 
     * @param account
     *            The bank account name
     * 
     * @param amount
     *            The amount of money to be withdrawn
     */
    private static void withdraw(String account, float amount) {
        
        logger.info(String.format("Withdrawing amount:: %f into bank account:: %s", amount, account));
        
        AccountBalance b = manager.read(account).isPresent() ? (AccountBalance) manager.read(account).get()
                : new AccountBalance(0);
        
        AccountBalance bnew = new AccountBalance(b.getBalance() - amount);
        
        manager.write(account, bnew);
        
        logger.info(String.format("Withdrew amount:: %f into bank account:: %s, new amount:: %f", amount, account,
                ((AccountBalance) manager.read(account).get()).getBalance()));
    }
    
    /**
     * Initializer of the simulation
     */
    @SuppressWarnings("unchecked")
    private static void setupBankAccounts12() {
        
        // create the bank accounts to operate on
        manager.make("Account1");
        manager.make("Account2");
        
        // make initial states
        manager.write("Account1", new AccountBalance(500.0F));
        manager.write("Account2", new AccountBalance(1500.0F));
    }
    
    /**
     * <p>
     * Testing out the FOOP using a single thread
     */
    @Test
    public void testDrive1() {
        
        logger.info(String.format("Initiating BankDriver..."));
        
        setupBankAccounts12();
        
        logger.info(String.format("Initially:: Acc1:: %f, Acc2:: %f",
                ((AccountBalance) manager.read("Account1").get()).getBalance(),
                ((AccountBalance) manager.read("Account2").get()).getBalance()));
        
        Transaction t = ts.newTransaction("T1", manager).addWriteSetMembers("Account1", "Account2")
                .addReadSetMembers("Account1", "Account2").addTransactionOperation((Transaction tt) -> {
                    
                    withdraw("Account2", 500.00F);
                    deposit("Account1", 500.00F);
                    
                    return true;
                }).get();
        
        t.start();
        
        try {
            
            // wait on the transaction to complete
            t.join();
        } catch (InterruptedException e) {
            
            logger.error(e.getMessage(), e);
        }
        
        logger.info(String.format("After transaction T1 :: Acc1:: %f, Acc2:: %f",
                ((AccountBalance) manager.read("Account1").get()).getBalance(),
                ((AccountBalance) manager.read("Account2").get()).getBalance()));
        
        logger.info("Finishing up BankDriver...");
    }
}
