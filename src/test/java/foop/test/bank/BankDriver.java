/**
 * Project: Foops_1
 * Package: foop.test.bank
 * File: BankDriver.java
 * 
 * @author sidmishraw
 *         Last modified: Oct 3, 2017 4:35:05 PM
 */
package foop.test.bank;

import java.util.concurrent.CountDownLatch;

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
        
        Transaction t = ts.newTransaction("T1", manager)
                .addWriteSetMembers("Account1", "Account2")
                .addReadSetMembers("Account1", "Account2")
                .addTransactionOperation(() -> {
                    
                    withdraw("Account2", 500.00F);
                    deposit("Account1", 500.00F);
                    
                    return true;
                })
                .get();
        
        t.setName("T1");
        
        t.setLatch(new CountDownLatch(1));
        
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
    
    /**
     * <p>
     * Test drive 2: This time we have 2 transactions that will be taking part
     * in the workflow.
     * T1 :: withdraw 500 from Account2 and deposit into Account1
     * T2 :: withdraw 100 from Account1 and deposit into Account2
     * 
     * Since both these transactions are conflicing in nature, it will be a good
     * show!!!
     */
    @Test
    public void testDrive2() {
        
        logger.info(String.format("Initiating test driver 2..."));
        
        // set up the bank accounts Account1 and Account2
        setupBankAccounts12();
        
        logger.info(String.format("Initially:: Acc1:: %f, Acc2:: %f",
                ((AccountBalance) manager.read("Account1").get()).getBalance(),
                ((AccountBalance) manager.read("Account2").get()).getBalance()));
        
        Transaction t1 = ts.newTransaction("T1", manager)
                .addWriteSetMembers("Account1", "Account2")
                .addReadSetMembers("Account1", "Account2")
                .addTransactionOperation(() -> {
                    
                    withdraw("Account2", 500.00F);
                    deposit("Account1", 500.00F);
                    
                    return true;
                })
                .get();
        
        Transaction t2 = ts.newTransaction("T2", manager)
                .addWriteSetMembers("Account1", "Account2")
                .addReadSetMembers("Account1", "Account2")
                .addTransactionOperation(() -> {
                    
                    withdraw("Account1", 100.00F);
                    deposit("Account2", 100.00F);
                    
                    return true;
                })
                .get();
        
        CountDownLatch latch = new CountDownLatch(2);
        
        // for simplicity sake, renaming threads to match their description
        // this is unnessecary but ---
        t1.setName("T1");
        t2.setName("T2");
        
        // add the t1 to latches
        t1.setLatch(latch);
        t2.setLatch(latch);
        
        t1.start();
        t2.start();
        
        try {
            
            // wait till all the transactions are done
            latch.await();
        } catch (InterruptedException e) {
            
            logger.error(e.getMessage(), e);
        }
        
        logger.info(String.format("Finally:: Acc1:: %f, Acc2:: %f",
                ((AccountBalance) manager.read("Account1").get()).getBalance(),
                ((AccountBalance) manager.read("Account2").get()).getBalance()));
        
        logger.info(String.format("Finishing up test driver 2 of Bank Driver ..."));
    }
}
