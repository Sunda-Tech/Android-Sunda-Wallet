package com.ibnux.nuxwallet.data;

/******************************************************************************\
 * GNU GENERAL PUBLIC LICENSE                                                 *
 * Version 3, 29 June 2007                                                    *
 * Ibnu Maksum https://github.com/ibnux/                                      *
 ******************************************************************************
 * This source and program come as is, WITHOUT ANY WARRANTY and/or WITHOUT    *
 * ANY IMPLIED WARRANTY.                                                      *
 \******************************************************************************/

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Dompet {
    @Id
    public long id;
    public String nama;
    public String catatan;
    public long saldo;
    public String dompetID;
    public String alamat;
    public String publicKey;
    public boolean isMe;
    public String secretPhrase;

    public boolean isMe(){
        return (secretPhrase!=null && !secretPhrase.isEmpty());
    }

    @Override
    public String toString() {
        return alamat;
    }
}
