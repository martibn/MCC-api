package com.mcc.api.service;

import com.mcc.api.model.CardType;
import com.mcc.api.repository.CardTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardTypeService {

    private final CardTypeRepository cardTypeRepository;

    public List<CardType> getEnabled() {
        return cardTypeRepository.findByEnabledTrue();
    }

    public List<CardType> getAll() {
        return cardTypeRepository.findAll();
    }

    public CardType create(String name) {
        CardType ct = new CardType();
        ct.setName(name.toUpperCase());
        ct.setEnabled(true);
        return cardTypeRepository.save(ct);
    }
}
