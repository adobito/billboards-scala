package com.jluzon.billboards.jdbc.dto

import java.sql.Timestamp



case class Email(emailId: Int ,emailAddress: String, active: Boolean, signupDate: Timestamp ) 