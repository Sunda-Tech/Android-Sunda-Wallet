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
public class Server {
    @Id
    public long id;
    public String url;

    public Server(String server){
        this.url = server;
    }
}
