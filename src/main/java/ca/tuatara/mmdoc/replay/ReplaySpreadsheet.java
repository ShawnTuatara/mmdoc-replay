package ca.tuatara.mmdoc.replay;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ca.tuatara.mmdoc.replay.data.Replay;
import ca.tuatara.mmdoc.replay.data.command.GameOver;

public class ReplaySpreadsheet {

    private List<Replay> replays;

    public ReplaySpreadsheet(List<Replay> replays) {
        this.replays = replays;
    }

    private void createMatchSheet(Workbook workbook) {
        CreationHelper creationHelper = workbook.getCreationHelper();
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-MM-dd hh:mm"));

        int numOfColumns = 0;

        Sheet sheet = workbook.createSheet("matches");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(numOfColumns++).setCellValue("Date Played");
        headerRow.createCell(numOfColumns++).setCellValue("Local Draw");
        headerRow.createCell(numOfColumns++).setCellValue("Hot Seat");
        headerRow.createCell(numOfColumns++).setCellValue("Player Elo");
        headerRow.createCell(numOfColumns++).setCellValue("Opponent Name");
        headerRow.createCell(numOfColumns++).setCellValue("Opponent Elo");

        // Game Over Details
        headerRow.createCell(numOfColumns++).setCellValue("Game Won");
        headerRow.createCell(numOfColumns++).setCellValue("Player Elo");
        headerRow.createCell(numOfColumns++).setCellValue("Opponent Elo");

        headerRow.createCell(numOfColumns++).setCellValue("XP");
        headerRow.createCell(numOfColumns++).setCellValue("Victory Bonus");
        headerRow.createCell(numOfColumns++).setCellValue("Endurance Bonus");
        headerRow.createCell(numOfColumns++).setCellValue("XP Boost");

        headerRow.createCell(numOfColumns++).setCellValue("Gold");
        headerRow.createCell(numOfColumns++).setCellValue("Damage Bonus");
        headerRow.createCell(numOfColumns++).setCellValue("Bonus Type");
        headerRow.createCell(numOfColumns++).setCellValue("Bonus Gold");
        headerRow.createCell(numOfColumns++).setCellValue("Gold Boost");

        for (int rowIndex = 1; rowIndex < replays.size(); rowIndex++) {
            Replay replay = replays.get(rowIndex);
            Row row = sheet.createRow(rowIndex);

            addReplayDetails(replay, row, dateStyle);

            addGameOverDetails(replay, row, 6);
        }

        for (int columnIndex = 0; columnIndex < numOfColumns; columnIndex++) {
            sheet.autoSizeColumn(columnIndex++);
        }
    }

    private void addGameOverDetails(Replay replay, Row row, int cellIndex) {
        GameOver gameOver = replay.getCommand(GameOver.class);
        if (gameOver != null) {
            row.createCell(cellIndex++).setCellValue(gameOver.isWon() ? "Won" : "Loss");
            row.createCell(cellIndex++).setCellValue(gameOver.getPlayerElo());
            row.createCell(cellIndex++).setCellValue(gameOver.getOpponentElo());

            row.createCell(cellIndex++).setCellValue(gameOver.getXp());
            row.createCell(cellIndex++).setCellValue(gameOver.getVictoryBonus());
            row.createCell(cellIndex++).setCellValue(gameOver.getEnduranceBonus());
            row.createCell(cellIndex++).setCellValue(gameOver.getXpBoost());

            row.createCell(cellIndex++).setCellValue(gameOver.getGold());
            row.createCell(cellIndex++).setCellValue(gameOver.getDmgInflictedBonus());
            row.createCell(cellIndex++).setCellValue(gameOver.getBonusType() != null ? gameOver.getBonusType().name() : "");
            row.createCell(cellIndex++).setCellValue(gameOver.getBonusGold());
            row.createCell(cellIndex++).setCellValue(gameOver.getGoldBoost());
        } else {
            row.createCell(cellIndex++).setCellValue("Loss - Error");
        }
    }

    private void addReplayDetails(Replay replay, Row row, CellStyle dateStyle) {
        int cellIndex = 0;
        Cell datePlayedRow = row.createCell(cellIndex++);
        datePlayedRow.setCellValue(replay.getDatePlayed());
        datePlayedRow.setCellStyle(dateStyle);

        row.createCell(cellIndex++).setCellValue(replay.isEnableLocalDraw());
        row.createCell(cellIndex++).setCellValue(replay.isHotSeat());
        row.createCell(cellIndex++).setCellValue(replay.getPlayerElo());
        row.createCell(cellIndex++).setCellValue(replay.getOpponentName());
        row.createCell(cellIndex++).setCellValue(replay.getOpponentElo());
    }

    public void write(OutputStream outputStream) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        createMatchSheet(workbook);
        workbook.write(outputStream);
    }
}
