package com.Acrobot.ChestShop.Commands;

import com.Acrobot.ChestShop.ChestShop;
import com.Acrobot.ChestShop.Config.Config;
import com.Acrobot.ChestShop.Config.Language;
import com.Acrobot.ChestShop.DB.Transaction;
import com.Acrobot.ChestShop.Economy;
import com.avaje.ebean.PagingList;
import com.avaje.ebean.Query;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;

public class Transactions implements CommandExecutor {

    private final Map<CommandSender, PagingList<Transaction>> cachedLists = new WeakHashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayDeque<String> argsQueue = new ArrayDeque<>(Arrays.asList(args));
        if (argsQueue.isEmpty()) return false;

        if (argsQueue.peek().equalsIgnoreCase("page")) {
            int index;
            try {
                index = getIndex(argsQueue);
            } catch (Exception e) {
                return false;
            }
            handlePage(sender, index);
            return true;
        }

        Query<Transaction> query = buildQuery(argsQueue);
        if (query == null) return false;

        PagingList<Transaction> result = query.findPagingList(15);
        if (printPage(sender, result, 1)) {
            cachedLists.put(sender, result);
        }
        return true;
    }

    private void handlePage(CommandSender sender, int index) {
        PagingList<Transaction> pagingList = cachedLists.get(sender);
        if (pagingList == null) {
            sender.sendMessage(Config.getLocal(Language.NO_QUERY));
            return;
        }

        printPage(sender, pagingList, index);
    }

    private Query<Transaction> buildQuery(Queue<String> argsQueue) {
        Query<Transaction> query = ChestShop.getDB().find(Transaction.class);
        while (!argsQueue.isEmpty()) {
            String param = argsQueue.poll();

            switch (param.toLowerCase()) {
                case "from": {
                    String shopUser = argsQueue.poll();
                    if (shopUser == null) return null;
                    query = query.where().eq("LOWER(shopUser)", shopUser.toLowerCase()).query();
                    break;
                }
                case "to": {
                    String shopOwner = argsQueue.poll();
                    if (shopOwner == null) return null;
                    query = query.where().eq("LOWER(shopOwner)", shopOwner.toLowerCase()).query();
                    break;
                }
                case "before":  {
                    String date = argsQueue.poll();
                    long timestamp;
                    try {
                        timestamp = parseDate(date);
                    } catch (Exception e) {
                        return null;
                    }
                    query = query.where().lt("sec", timestamp).query();
                    break;
                }
                case "after": {
                    String date = argsQueue.poll();
                    long timestamp;
                    try {
                        timestamp = parseDate(date);
                    } catch (Exception e) {
                        return null;
                    }
                    query = query.where().gt("sec", timestamp).query();
                    break;
                }
                default:
                    return null;
            }
        }

        return query.orderBy().desc("sec");
    }

    private boolean printPage(CommandSender sender, PagingList<Transaction> pagingList, int index) {
        if (index < 1) {
            sender.sendMessage(Config.getLocal(Language.NO_RESULTS_FOR_QUERY));
            return false;
        }

        List<Transaction> transactions = pagingList.getPage(index - 1).getList();
        if (transactions.isEmpty()) {
            sender.sendMessage(Config.getLocal(Language.NO_RESULTS_FOR_QUERY));
            return false;
        }

        sender.sendMessage(Config.getLocal(Language.QUERY_RESULTS)
                .replace("%page", String.valueOf(index))
                .replace("%total", String.valueOf(pagingList.getTotalPageCount())));

        for (Transaction transaction : transactions) {
            String message = Config.getLocalNoPrefix(Language.QUERY_ROW)
                    .replace("%datetime", formatDateTime(transaction.getSec()))
                    .replace("%user", transaction.getShopUser())
                    .replace("%owner", transaction.getShopOwner())
                    .replace("%mode", transaction.isBuy() ? "BUY" : "SELL")
                    .replace("%amount", String.valueOf(transaction.getAmount()))
                    .replace("%item", Material.getMaterial(transaction.getItemID()).toString())
                    .replace("%price", Economy.formatBalance(transaction.getPrice()));
            sender.sendMessage(message);
        }
        sender.sendMessage(Config.getLocalNoPrefix(Language.QUERY_SWITCH));

        return true;
    }

    private int getIndex(Queue<String> argsQueue) {
        argsQueue.poll();
        String page = argsQueue.poll();
        return Integer.parseInt(String.valueOf(page));
    }

    private long parseDate(String date) {
        if (date == null)
            throw new NullPointerException();
        String[] split = date.split("-");
        if (split.length != 3)
            throw new IllegalArgumentException();
        int year = Integer.parseUnsignedInt(split[0]);
        int month = Integer.parseUnsignedInt(split[1]);
        int day = Integer.parseUnsignedInt(split[2]);
        return LocalDateTime.of(year, month, day, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli() / 1000;
    }

    private String formatDateTime(long epochSec) {
        return formatter.format(LocalDateTime.ofEpochSecond(epochSec, 0, ZoneOffset.UTC));
    }

}
