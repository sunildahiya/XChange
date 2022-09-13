package org.knowm.xchange.coindcx.service;

import org.knowm.xchange.coindcx.CoindcxAuthenticated;
import org.knowm.xchange.coindcx.CoindcxExchange;
import org.knowm.xchange.coindcx.dto.trade.CoindcxBalance;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.service.account.AccountService;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class CoindcxAccountService extends CoindcxAccountServiceRaw implements AccountService {

    public CoindcxAccountService(CoindcxExchange exchange, CoindcxAuthenticated coindcx) {
        super(exchange, coindcx);
    }

    @Override
    public AccountInfo getAccountInfo() throws IOException {
        List<CoindcxBalance> coindcxBalances = getBalances();
        HashSet<String> currencies = new HashSet<>();
        coindcxBalances = coindcxBalances.stream().filter(coindcxBalance -> {
            if (currencies.contains(coindcxBalance.currency.toLowerCase()))
                return false;
            else
                currencies.add(coindcxBalance.currency.toLowerCase());
            return true;
        }).collect(Collectors.toList());

        List<Balance> balances = coindcxBalances.stream().map(
                coindcxBalance ->
                        new Balance.Builder()
                                .currency(new Currency(coindcxBalance.currency))
                                .available(coindcxBalance.balance)
                                .frozen(coindcxBalance.lockedBalance)
                                .total(coindcxBalance.balance.add(coindcxBalance.lockedBalance))
                                .build()
        ).collect(Collectors.toList());

        Wallet wallet = Wallet.Builder.from(balances).build();
        return new AccountInfo(wallet);
    }
}
