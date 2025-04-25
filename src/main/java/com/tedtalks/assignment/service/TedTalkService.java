package com.tedtalks.assignment.service;

import com.tedtalks.assignment.dto.FileImportResponse;
import com.tedtalks.assignment.dto.InfluentialSpeakerDto;
import com.tedtalks.assignment.dto.InfluentialSpeakerPerYearDto;
import com.tedtalks.assignment.dto.TedTalkDto;
import com.tedtalks.assignment.entity.TedTalk;
import com.tedtalks.assignment.mapper.TedTalkMapper;
import com.tedtalks.assignment.repository.TedTalkRepository;
import com.tedtalks.assignment.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TedTalkService {

    private static final int BATCH_SIZE = 1000;
    private static final float VIEWS_WEIGHT = 0.7f;
    private static final float LIKES_WEIGHT = 0.3f;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");


    private final TedTalkRepository tedTalkRepository;
    private final TedTalkMapper tedTalkMapper;

    public FileImportResponse importCsv(final MultipartFile file) throws IOException  {
        long successfulRecordCount = 0L;
        long recordCount = 0L;

        try (
                Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())
        ) {

            final List<TedTalk> tedTalks = new ArrayList<>();

            for (CSVRecord csvRecord : csvParser) {
                recordCount++;

                if (!FileUtil.verifyCsvRecord(csvRecord, DATE_FORMATTER)) {
                    continue;
                }

                tedTalks.add(new TedTalk(csvRecord.get(0),
                        csvRecord.get(1),
                        YearMonth.parse(csvRecord.get(2), DATE_FORMATTER).atDay(1),
                        Long.parseLong(csvRecord.get(3)),
                        Long.parseLong(csvRecord.get(4)),
                        csvRecord.get(5)));

                if (tedTalks.size() == BATCH_SIZE) {
                    successfulRecordCount += BATCH_SIZE;
                    tedTalkRepository.saveAll(tedTalks);
                    tedTalks.clear();
                }
            }

            // Check if the file has been read completely and save the leftovers
            if (!CollectionUtils.isEmpty(tedTalks)) {
                successfulRecordCount += tedTalks.size();
                tedTalkRepository.saveAll(tedTalks);
                tedTalks.clear();
            }
        }

        return new FileImportResponse(recordCount, successfulRecordCount, recordCount - successfulRecordCount);
    }

    public Page<TedTalkDto> retrieveSpeechByYear(final Integer eventYear, final Pageable pageable) {
        return tedTalkRepository.findTedTalksByEventDateBetween(LocalDate.of(eventYear, Month.JANUARY, 1),
                        LocalDate.of(eventYear, Month.DECEMBER, 31), pageable)
                .map(tedTalkMapper::toDto);
    }

    public Page<TedTalkDto> retrieveSpeechByAuthor(final String authorName, final Pageable pageable) {
        return tedTalkRepository.findTedTalksByAuthor(authorName, pageable)
                .map(tedTalkMapper::toDto);
    }

    public Page<TedTalkDto> searchTedTalks(final String searchTerm, final Pageable pageable) {
        return tedTalkRepository.searchTedTalks(searchTerm, pageable)
                .map(tedTalkMapper::toDto);
    }

    public Page<InfluentialSpeakerDto> findInfluentialSpeakers(final Pageable pageable) {
        return tedTalkRepository.findInfluentialSpeakers(VIEWS_WEIGHT, LIKES_WEIGHT, pageable)
                .map(tedTalkMapper::toInfluentialSpeakerDto);
    }

    public List<InfluentialSpeakerPerYearDto> findMostInfluentialTalksPerYear() {
        return tedTalkRepository.findMostInfluentialTalksPerYear(VIEWS_WEIGHT, LIKES_WEIGHT)
                .stream()
                .map(tedTalkMapper::toInfluentialSpeakerPerYearDto)
                .toList();
    }

}
