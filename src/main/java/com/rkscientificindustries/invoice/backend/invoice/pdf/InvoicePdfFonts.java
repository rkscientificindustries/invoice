package com.rkscientificindustries.invoice.backend.invoice.pdf;

import org.openpdf.text.Font;

record InvoicePdfFonts(Font title,
                       Font companyName,
                       Font header,
                       Font body,
                       Font bodyBold) {
}


