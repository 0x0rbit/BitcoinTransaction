package edu.stanford.crypto.cs251.transactions;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.io.File;
import java.math.BigInteger;
import java.net.UnknownHostException;

import static org.bitcoinj.script.ScriptOpCodes.*;

/**
 * Created by bbuenz on 24.09.15.
 */
public class LinearEquationTransaction extends ScriptTransaction {
    // TODO: Problem 2 
	private BigInteger x;
	private BigInteger y;
	// GMU ID: G01037251
	private byte[] gmuid1= encode(BigInteger.valueOf(103));  // Part1 = 0103  
	private byte[] gmuid2= encode(BigInteger.valueOf(7251)); // Part2 = 7251
	
    public LinearEquationTransaction(NetworkParameters parameters, File file, String password) {
        super(parameters, file, password);
        x=BigInteger.valueOf(3677);
        y=BigInteger.valueOf(3574);
        
    }

    @Override
    public Script createInputScript() {
        // TODO: Create a script that can be spend by two numbers x and y such that x+y=first 4 digits of your gmuid and |x-y|=last 4 digits of your gmuid (perhaps +1)
    	 ScriptBuilder builder = new ScriptBuilder();
         builder.op(OP_2DUP);         // Double duplicate x and y 
         builder.op(OP_NEGATE);       // Negate y on top of stack
         builder.op(OP_ADD);          // x + (-y)
         builder.data(gmuid1);        // Put Part1 = 103 on stack
         builder.op(OP_NUMEQUALVERIFY); // Check if 3677 + (-3574) = 103 and remove from stack 
         builder.op(OP_NEGATE);       // Negate y on top of stack
         builder.op(OP_SUB);           // x - (-y)
         builder.data(gmuid2);         // Put Part1 = 103 on stack
         builder.op(OP_NUMEQUAL);      // Check if 3677 - (-3574) = 7251 and return True/False
         return builder.build();
    }

    @Override
    public Script createRedemptionScript(Transaction unsignedScript) {
        // TODO: Create a spending script

         ScriptBuilder builder = new ScriptBuilder();
         builder.data(encode(x));      // Put x = 3677 on stack
         builder.data(encode(y));      // Put y = 3574 on stack
         return builder.build();
    }

    private byte[] encode(BigInteger bigInteger) {
        return Utils.reverseBytes(Utils.encodeMPI(bigInteger, false));
    }
}
