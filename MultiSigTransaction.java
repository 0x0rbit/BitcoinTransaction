package edu.stanford.crypto.cs251.transactions;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.bitcoinj.script.ScriptOpCodes.*;

/**
 * Created by bbuenz on 24.09.15.
 */
public class MultiSigTransaction extends ScriptTransaction {
    // TODO: Problem 3
    ECKey bank;
    ECKey cust1;
    ECKey cust2;
    ECKey cust3;
    Script redeemScript;
	
    public MultiSigTransaction(NetworkParameters parameters, File file, String password) {
        super(parameters, file, password);
        // Keys need to be decompressed to be compatible for signing the transactions later
        bank = new ECKey().decompress();
        cust1 = new ECKey().decompress();
        cust2 = new ECKey().decompress();
        cust3 = new ECKey().decompress();
    }

    @Override
    public Script createInputScript() {
        // TODO: Create a script that can be spend using signatures from the bank and one of the customers
    	 ScriptBuilder builder = new ScriptBuilder();
    	 builder.smallNum(1);             // 1 out 3 Multi-signature  
    	 builder.data(cust1.getPubKey());     
    	 builder.data(cust2.getPubKey());
    	 builder.data(cust3.getPubKey());
    	 builder.smallNum(3);
    	 builder.op(OP_CHECKMULTISIGVERIFY);  // Check multisig for the 3 customers and remove from stack
    	 builder.data(bank.getPubKey());      // Put Bank's public key on stack
    	 builder.op(OP_CHECKSIG);         // Check bank's signature: Mandatory
    	 redeemScript = builder.build();  // Create a redeemScript 
    	 Script outscript = ScriptBuilder.createP2SHOutputScript(redeemScript);  // Create P2SH using redeem script passed.    
     	 return outscript;
    }

    @Override
    public Script createRedemptionScript(Transaction unsignedTransaction) {
        // Please be aware of the CHECK_MULTISIG bug!
        // TODO: Create a spending script
     	
    	// Create sighash by passing the redeem Script instead of Public key, since it is a P2SH transaction
    	Sha256Hash sighash = unsignedTransaction.hashForSignature(0, redeemScript, Transaction.SigHash.ALL, false); 
     	
    	// Create bank's signature using the sighash calculated
    	ECKey.ECDSASignature ecdsaSigBank = bank.sign(sighash);  
     	TransactionSignature txSigBank = new TransactionSignature(ecdsaSigBank, Transaction.SigHash.ALL, false);
        
     	// Create Customer 1's signature using the sighash calculated
     	ECKey.ECDSASignature ecdsaSigCust1 = cust1.sign(sighash);
     	TransactionSignature txSigCust1 = new TransactionSignature(ecdsaSigCust1, Transaction.SigHash.ALL, false);
        
     	// Create Customer 2's signature using the sighash calculated
     	ECKey.ECDSASignature ecdsaSigCust2 = cust2.sign(sighash);
     	TransactionSignature txSigCust2 = new TransactionSignature(ecdsaSigCust2, Transaction.SigHash.ALL, false);
     
     	// Create Customer 3's signature using the sighash calculated
     	ECKey.ECDSASignature ecdsaSigCust3 = cust3.sign(sighash);
     	TransactionSignature txSigCust3 = new TransactionSignature(ecdsaSigCust2, Transaction.SigHash.ALL, false);
     	
     	 ScriptBuilder builder = new ScriptBuilder();
     	 // Bank's signature is mandatory to redeem
         builder.data(txSigBank.encodeToBitcoin());     // Put Bank's signature on stack
//         builder.data(txSigCust2.encodeToBitcoin());   // Check False case: Customer2 signing instead of bank
     	 builder.smallNum(0);  // Work around a bug in CHECKMULTISIG that is now a required part of the protocol.
     	 
     	 // Only 1 customer's signature is required to redeem      	 
     	 builder.data(txSigCust1.encodeToBitcoin());     // Put Cust1's signature on stack
//     	 builder.data(txSigCust2.encodeToBitcoin());     // Put Cust2's signature on stack
//     	 builder.data(txSigCust3.encodeToBitcoin());     // Put Cust3's signature on stack
         builder.data(redeemScript.getProgram());        // Put the redeemScript on stack 
         return builder.build();                         // Return Input Script
    }

}
