package com.expensetracker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class CsvUtilTest {
    @Test void escapesQuotesAndCommas(){assertEquals("\"a, b\",\"say \"\"hello\"\"\"\r\n",CsvUtil.row("a, b","say \"hello\""));}
    @Test void preventsFormulaInjectionAndPreservesNegativeNumbers(){assertEquals("\"'=1+1\",\"-12.50\"\r\n",CsvUtil.row("=1+1","-12.50"));}
}
