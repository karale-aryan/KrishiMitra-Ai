-- =====================================================
-- KrishiMitra AI - Government Scheme Seed Data
-- Real Indian agriculture schemes with eligibility criteria
-- =====================================================

INSERT INTO government_schemes (id, scheme_name, scheme_name_hi, description, description_hi, scheme_type, eligibility_criteria, benefits, application_url, valid_from, valid_until, is_active) VALUES

-- 1. PM-Kisan
(gen_random_uuid(),
 'Pradhan Mantri Kisan Samman Nidhi (PM-KISAN)',
 'प्रधानमंत्री किसान सम्मान निधि (PM-KISAN)',
 'Direct income support of Rs. 6,000 per year to all landholding farmer families across the country, paid in three equal installments of Rs. 2,000.',
 'देश भर के सभी भूमिधारक किसान परिवारों को प्रति वर्ष 6,000 रुपये की प्रत्यक्ष आय सहायता, जो 2,000 रुपये की तीन समान किस्तों में दी जाती है।',
 'FINANCIAL_SUPPORT',
 '{"states": ["ALL"], "farmerTypes": ["ALL"], "maxLandHolding": 999.0, "incomeCategories": ["ALL"]}',
 'Rs. 6,000 per year in three installments of Rs. 2,000 each, directly transferred to bank accounts.',
 'https://pmkisan.gov.in',
 '2019-02-01', '2030-12-31', true),

-- 2. PMFBY
(gen_random_uuid(),
 'Pradhan Mantri Fasal Bima Yojana (PMFBY)',
 'प्रधानमंत्री फसल बीमा योजना (PMFBY)',
 'Comprehensive crop insurance scheme providing financial support to farmers suffering crop loss/damage from natural calamities, pests, and diseases.',
 'प्राकृतिक आपदाओं, कीटों और रोगों से फसल हानि/क्षति से पीड़ित किसानों को वित्तीय सहायता प्रदान करने वाली व्यापक फसल बीमा योजना।',
 'INSURANCE',
 '{"states": ["ALL"], "farmerTypes": ["ALL"], "maxLandHolding": 999.0, "incomeCategories": ["ALL"], "requiresLoanee": false}',
 'Kharif crops: 2% premium, Rabi crops: 1.5% premium, Commercial crops: 5% premium. Remaining premium paid by government.',
 'https://pmfby.gov.in',
 '2016-04-01', '2030-12-31', true),

-- 3. Kisan Credit Card (KCC)
(gen_random_uuid(),
 'Kisan Credit Card (KCC)',
 'किसान क्रेडिट कार्ड (KCC)',
 'Provides farmers with affordable short-term credit for cultivation, post-harvest expenses, and consumption needs at subsidized interest rates.',
 'किसानों को खेती, फसल कटाई के बाद के खर्चों और उपभोग की जरूरतों के लिए रियायती ब्याज दरों पर किफायती अल्पकालिक ऋण प्रदान करता है।',
 'CREDIT',
 '{"states": ["ALL"], "farmerTypes": ["ALL"], "maxLandHolding": 999.0, "incomeCategories": ["ALL"]}',
 'Credit limit up to Rs. 3 lakh at 4% interest rate (with prompt repayment). Interest subvention of 2% by GoI + 3% for prompt repayment.',
 'https://www.pmkisan.gov.in/KCC',
 '1998-08-01', '2030-12-31', true),

-- 4. Soil Health Card Scheme
(gen_random_uuid(),
 'Soil Health Card Scheme',
 'मृदा स्वास्थ्य कार्ड योजना',
 'Provides soil health cards to farmers carrying crop-wise recommendations on nutrients and fertilizers required for individual farms.',
 'किसानों को मृदा स्वास्थ्य कार्ड प्रदान करता है जिसमें व्यक्तिगत खेतों के लिए आवश्यक पोषक तत्वों और उर्वरकों पर फसलवार सिफारिशें होती हैं।',
 'INPUT_SUBSIDY',
 '{"states": ["ALL"], "farmerTypes": ["ALL"], "maxLandHolding": 999.0, "incomeCategories": ["ALL"]}',
 'Free soil testing every 2 years with detailed soil health card and crop-specific fertilizer recommendations.',
 'https://soilhealth.dac.gov.in',
 '2015-02-19', '2030-12-31', true),

-- 5. National Food Security Mission (NFSM)
(gen_random_uuid(),
 'National Food Security Mission (NFSM)',
 'राष्ट्रीय खाद्य सुरक्षा मिशन (NFSM)',
 'Aims to increase production of rice, wheat, pulses, and coarse cereals through area expansion and productivity enhancement.',
 'क्षेत्र विस्तार और उत्पादकता वृद्धि के माध्यम से चावल, गेहूं, दलहन और मोटे अनाज के उत्पादन में वृद्धि करने का लक्ष्य।',
 'INPUT_SUBSIDY',
 '{"states": ["ALL"], "farmerTypes": ["SMALL", "MARGINAL"], "maxLandHolding": 5.0, "incomeCategories": ["BELOW_1_LAKH", "ONE_TO_THREE_LAKH", "THREE_TO_FIVE_LAKH"]}',
 'Subsidized seeds, micro-nutrients, soil ameliorants, INM/IPM demonstrations, farm machinery, and training programs.',
 'https://nfsm.gov.in',
 '2007-08-01', '2030-12-31', true),

-- 6. Rashtriya Krishi Vikas Yojana (RKVY)
(gen_random_uuid(),
 'Rashtriya Krishi Vikas Yojana (RKVY-RAFTAAR)',
 'राष्ट्रीय कृषि विकास योजना (RKVY-RAFTAAR)',
 'Incentivizes states to increase public investment in agriculture by providing flexibility and autonomy in planning and execution.',
 'राज्यों को योजना बनाने और क्रियान्वयन में लचीलापन और स्वायत्तता प्रदान करके कृषि में सार्वजनिक निवेश बढ़ाने के लिए प्रोत्साहित करता है।',
 'INFRASTRUCTURE',
 '{"states": ["ALL"], "farmerTypes": ["ALL"], "maxLandHolding": 999.0, "incomeCategories": ["ALL"]}',
 'Funding for agri-infrastructure, value chain projects, innovation, and agri-preneurship. Up to Rs. 25 lakh for individual projects.',
 'https://rkvy.nic.in',
 '2007-08-01', '2030-12-31', true),

-- 7. PM-KUSUM (Solar pumps)
(gen_random_uuid(),
 'Pradhan Mantri Kisan Urja Suraksha evam Utthaan Mahabhiyan (PM-KUSUM)',
 'प्रधानमंत्री किसान ऊर्जा सुरक्षा एवं उत्थान महाभियान (PM-KUSUM)',
 'Provides subsidized solar pumps and solarization of grid-connected agricultural pumps to ensure reliable power supply for irrigation.',
 'सिंचाई के लिए विश्वसनीय बिजली आपूर्ति सुनिश्चित करने के लिए रियायती सौर पंप और ग्रिड से जुड़े कृषि पंपों का सौरीकरण प्रदान करता है।',
 'INFRASTRUCTURE',
 '{"states": ["ALL"], "farmerTypes": ["ALL"], "maxLandHolding": 999.0, "incomeCategories": ["ALL"]}',
 'Up to 60% subsidy on solar pumps (30% CFA + 30% State). Component A: 10,000 MW solar plants. Component B: 20 lakh standalone solar pumps. Component C: 15 lakh grid-connected solar pumps.',
 'https://pmkusum.mnre.gov.in',
 '2019-03-01', '2030-12-31', true),

-- 8. Mission for Integrated Development of Horticulture (MIDH)
(gen_random_uuid(),
 'Mission for Integrated Development of Horticulture (MIDH)',
 'एकीकृत बागवानी विकास मिशन (MIDH)',
 'Promotes holistic growth of the horticulture sector including fruits, vegetables, root & tuber crops, mushrooms, spices, flowers, aromatic plants, coconut, cashew, and cocoa.',
 'फल, सब्जियां, कंद फसलें, मशरूम, मसाले, फूल, सुगंधित पौधे, नारियल, काजू और कोको सहित बागवानी क्षेत्र की समग्र वृद्धि को बढ़ावा देता है।',
 'INPUT_SUBSIDY',
 '{"states": ["ALL"], "farmerTypes": ["ALL"], "maxLandHolding": 999.0, "incomeCategories": ["ALL"], "cropTypes": ["HORTICULTURE"]}',
 'Subsidies for planting material, rejuvenation of old orchards, protected cultivation (polyhouses, shade nets), organic farming, INM/IPM, and post-harvest management.',
 'https://midh.gov.in',
 '2014-04-01', '2030-12-31', true),

-- 9. National Mission on Sustainable Agriculture (NMSA)
(gen_random_uuid(),
 'National Mission on Sustainable Agriculture (NMSA)',
 'राष्ट्रीय सतत कृषि मिशन (NMSA)',
 'Promotes sustainable agriculture practices through climate-resilient approaches including water use efficiency, soil health management, and rainfed area development.',
 'जल उपयोग दक्षता, मृदा स्वास्थ्य प्रबंधन और वर्षा सिंचित क्षेत्र विकास सहित जलवायु-प्रतिरोधी दृष्टिकोणों के माध्यम से टिकाऊ कृषि पद्धतियों को बढ़ावा देता है।',
 'INFRASTRUCTURE',
 '{"states": ["ALL"], "farmerTypes": ["SMALL", "MARGINAL"], "maxLandHolding": 2.0, "incomeCategories": ["BELOW_1_LAKH", "ONE_TO_THREE_LAKH"]}',
 'Micro-irrigation subsidy (55% for small/marginal farmers), soil health management support, rainfed area development assistance.',
 'https://nmsa.dac.gov.in',
 '2014-04-01', '2030-12-31', true),

-- 10. eNAM (Electronic National Agriculture Market)
(gen_random_uuid(),
 'Electronic National Agriculture Market (eNAM)',
 'इलेक्ट्रॉनिक राष्ट्रीय कृषि बाजार (eNAM)',
 'Pan-India electronic trading portal networking existing APMC mandis to create a unified national market for agricultural commodities.',
 'कृषि उपज के लिए एक एकीकृत राष्ट्रीय बाजार बनाने के लिए मौजूदा APMC मंडियों को जोड़ने वाला अखिल भारतीय इलेक्ट्रॉनिक व्यापार पोर्टल।',
 'MARKETING',
 '{"states": ["ALL"], "farmerTypes": ["ALL"], "maxLandHolding": 999.0, "incomeCategories": ["ALL"]}',
 'Transparent price discovery, online bidding, direct payment to farmers, reduced intermediaries, wider market access, quality assaying at mandis.',
 'https://enam.gov.in',
 '2016-04-14', '2030-12-31', true),

-- 11. Paramparagat Krishi Vikas Yojana (PKVY) - Organic Farming
(gen_random_uuid(),
 'Paramparagat Krishi Vikas Yojana (PKVY)',
 'परम्परागत कृषि विकास योजना (PKVY)',
 'Promotes organic farming through cluster approach and Participatory Guarantee System (PGS) certification.',
 'क्लस्टर दृष्टिकोण और भागीदारी गारंटी प्रणाली (PGS) प्रमाणन के माध्यम से जैविक खेती को बढ़ावा देता है।',
 'INPUT_SUBSIDY',
 '{"states": ["ALL"], "farmerTypes": ["SMALL", "MARGINAL"], "maxLandHolding": 5.0, "incomeCategories": ["BELOW_1_LAKH", "ONE_TO_THREE_LAKH", "THREE_TO_FIVE_LAKH"]}',
 'Rs. 50,000 per hectare for 3 years for organic inputs, seeds, and certification. Cluster of 20 farmers with 50 acres minimum.',
 'https://pgsindia-ncof.gov.in',
 '2015-04-01', '2030-12-31', true),

-- 12. Sub-Mission on Agricultural Mechanization (SMAM)
(gen_random_uuid(),
 'Sub-Mission on Agricultural Mechanization (SMAM)',
 'कृषि मशीनीकरण पर उप-मिशन (SMAM)',
 'Promotes farm mechanization by providing subsidies for purchase of agricultural machinery and equipment.',
 'कृषि मशीनरी और उपकरणों की खरीद के लिए सब्सिडी प्रदान करके कृषि मशीनीकरण को बढ़ावा देता है।',
 'INPUT_SUBSIDY',
 '{"states": ["ALL"], "farmerTypes": ["SMALL", "MARGINAL"], "maxLandHolding": 5.0, "incomeCategories": ["BELOW_1_LAKH", "ONE_TO_THREE_LAKH", "THREE_TO_FIVE_LAKH"]}',
 '40-50% subsidy on agricultural machinery for small/marginal farmers. Custom Hiring Centers with 40% subsidy. Farm Machinery Banks with 80% subsidy.',
 'https://agrimachinery.nic.in',
 '2014-04-01', '2030-12-31', true);
