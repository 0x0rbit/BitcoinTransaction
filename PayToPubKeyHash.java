package edu.stanford.crypto.cs251.transactions;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.io.File;

import static org.bitcoinj.script.ScriptOpCodes.*;

/**
 * Created by bbuenz on 24.09.15.
 */
public class PayToPubKeyHash extends ScriptTransaction {
    // TODO: Problem 1
	private ECKey key;

    public PayToPubKeyHash(NetworkParameters parameters, File file, String password) {
        super(parameters, file, password);
        key = getWallet().getImportedKeys().get(0); // Use the imported private key
    }

    @Override
    public Script createInputScript() {
    	 ScriptBuilder builder = new ScriptBuilder();
         builder.op(OP_DUP);
         builder.op(OP_HASH160);
         builder.data(key.getPubKeyHash());
         builder.op(OP_EQUALVERIFY);
         builder.op(OP_CHECKSIG);
         return builder.build();
        // TODO: Create a P2PKH script
        // TODO: be sure to test this script on the mainnet using a vanity address
    }

    @Override
    public Script createRedemptionScript(Transaction unsignedTransaction) {
        // TODO: Redeem the P2PKH transaction
    	TransactionSignature txSig = sign(unsignedTransaction, key);

        ScriptBuilder builder = new ScriptBuilder();
        builder.data(txSig.encodeToBitcoin());
        builder.data(key.getPubKey());
        return builder.build();
    }
}
