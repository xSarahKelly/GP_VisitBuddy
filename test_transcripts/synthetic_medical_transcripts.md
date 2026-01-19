# Synthetic Medical Appointment Transcripts for Testing

 These are scripted, synthetic transcripts based on common GP consultation scenarios. They are NOT real patient data.

---

## Transcript 1: Antibiotic Prescription (Simple)

**Scenario:** Patient with chest infection, prescribed antibiotics

**Transcript:**
"Right, so based on your symptoms, I think you have a chest infection. I'm going to prescribe you amoxicillin. Take amoxicillin 500 milligrams three times a day for seven days. Make sure you take it with food to avoid stomach problems. If you develop a rash or have trouble breathing, stop taking it immediately and come back to see me. If your symptoms don't improve after three days, or if you develop a fever, come back. Otherwise, you should start feeling better within a few days. Any questions?"

**Expected Extraction:**
- **Medication:** Amoxicillin, 500mg, three times a day, for seven days, with food
- **Safety Warning:** "If you develop a rash or have trouble breathing, stop taking it immediately"
- **Safety Warning:** "If you develop a fever, come back"
- **Follow-up:** "If your symptoms don't improve after three days, come back"

---

## Transcript 2: Multiple Medications + Test

**Scenario:** Patient with high blood pressure and high cholesterol

**Transcript:**
"I'm going to start you on two medications. First, ramipril 5 milligrams once a day in the morning. Take it with breakfast. Second, atorvastatin 20 milligrams once a day at night. Take it with your evening meal. I also want you to have a blood test in two weeks to check your kidney function and liver function. Book that with reception. Come back to see me in four weeks to review how you're getting on. If you develop a persistent dry cough, let me know as that can be a side effect of ramipril. Also, if you get any muscle pain or weakness with the atorvastatin, stop taking it and contact me straight away."

**Expected Extraction:**
- **Medication 1:** Ramipril, 5mg, once a day (morning), with breakfast
- **Medication 2:** Atorvastatin, 20mg, once a day (night), with evening meal
- **Test:** Blood test, in two weeks, reason: check kidney and liver function
- **Follow-up:** Come back in four weeks
- **Safety Warning:** "If you develop a persistent dry cough, let me know"
- **Safety Warning:** "If you get any muscle pain or weakness with the atorvastatin, stop taking it and contact me straight away"

---

## Transcript 3: Urgent Referral + Safety Warnings

**Scenario:** Patient with concerning symptoms requiring urgent referral

**Transcript:**
"I'm a bit concerned about these symptoms, so I'm going to refer you urgently for a chest X-ray. This needs to be done as soon as possible, ideally this week. I'm also going to send you for some blood tests urgently. The hospital will contact you within the next few days to arrange these. In the meantime, if you develop any chest pain, or if you become short of breath, or if you develop a fever, go straight to A&E. Don't wait. If you feel generally unwell or if things get worse, call 999. I want you to come back and see me in one week regardless, so book that appointment now."

**Expected Extraction:**
- **Test:** Chest X-ray, urgent, this week
- **Test:** Blood tests, urgent, hospital will contact within next few days
- **Safety Warning:** "If you develop any chest pain, or if you become short of breath, or if you develop a fever, go straight to A&E"
- **Safety Warning:** "If you feel generally unwell or if things get worse, call 999"
- **Follow-up:** Come back in one week

---

## Transcript 4: Follow-up Only (No Medications)

**Scenario:** Patient review appointment, no new medications

**Transcript:**
"Good news, your test results came back normal. Everything looks fine. I want you to come back in three months for a review. Just book that with reception when you're ready. In the meantime, keep up with the exercise we discussed and try to maintain a healthy diet. If anything changes or if you have any concerns, don't hesitate to come back sooner."

**Expected Extraction:**
- **Follow-up:** Come back in three months, book with reception
- **Additional Notes:** "keep up with the exercise we discussed and try to maintain a healthy diet"
- **Additional Notes:** "If anything changes or if you have any concerns, don't hesitate to come back sooner"

---

## Transcript 5: Complex Medication Regime

**Scenario:** Patient with multiple conditions, complex medication schedule

**Transcript:"
"Right, let's review your medications. I want you to continue taking metformin 500 milligrams twice a day with meals. Keep taking your sertraline 50 milligrams once a day in the morning. I'm going to increase your omeprazole to 40 milligrams once a day, take it before breakfast. Also, I want you to start taking vitamin D supplements, 1000 international units once a day. You can get that over the counter. For your back pain, take ibuprofen 400 milligrams three times a day with food, but only for the next five days. Don't take it longer than that. If you get any stomach pain with the ibuprofen, stop taking it. Come back in six weeks to review your diabetes control."

**Expected Extraction:**
- **Medication 1:** Metformin, 500mg, twice a day, with meals, continue taking
- **Medication 2:** Sertraline, 50mg, once a day (morning), continue taking
- **Medication 3:** Omeprazole, 40mg, once a day, before breakfast
- **Medication 4:** Vitamin D, 1000 IU, once a day
- **Medication 5:** Ibuprofen, 400mg, three times a day, with food, for five days only
- **Safety Warning:** "If you get any stomach pain with the ibuprofen, stop taking it"
- **Follow-up:** Come back in six weeks

---

## Transcript 6: Women's Health + Test

**Scenario:** Patient with women's health concerns

**Transcript:**
"I'm going to prescribe you fluconazole, that's a single 150 milligram tablet. Just take it once, today. I also want you to have some swabs done to check for any other infections. You can do that here at the surgery, just book with the nurse. Come back in two weeks if the symptoms haven't cleared up. If you develop any severe side effects or an allergic reaction,  contact me immediately."

**Expected Extraction:**
- **Medication:** Fluconazole, 150mg, once (today), single dose
- **Test:** Swabs, at surgery, book with nurse
- **Follow-up:** Come back in two weeks if symptoms haven't cleared up
- **Safety Warning:** "If you develop any severe side effects or an allergic reaction,  contact me immediately"

---
## Transcript 7: Contraceptive Pill Prescription

**Scenario:** Patient starting combined oral contraceptive pill

**Transcript:**
"I'm going to start you on microgynon, that's a combined contraceptive pill. Take one tablet every day at the same time, ideally in the evening. Start taking it on the first day of your period. Take it for 21 days, then have a seven day break before starting the next pack. If you miss a pill, take it as soon as you remember, but use additional contraception for the next seven days. If you develop any chest pain, severe leg pain, or sudden shortness of breath, stop taking it immediately and go to A&E as these could be signs of a blood clot. Also, if you get severe headaches or visual disturbances, stop taking it and contact me. I want to see you back in three months to check how you're getting on with it. Book that appointment with reception."

**Expected Extraction:**
- **Medication:** Microgynon, one tablet, every day (evening), start first day of period, 21 days then 7 day break
- **Safety Warning:** "If you develop any chest pain, severe leg pain, or sudden shortness of breath, stop taking it immediately and go to A&E as these could be signs of a blood clot"
- **Safety Warning:** "If you get severe headaches or visual disturbances, stop taking it and contact me"
- **Follow-up:** Come back in three months, book with reception

---

## Transcript 8: Endometriosis Management

**Scenario:** Patient with suspected endometriosis, pain management and referral

**Transcript:**
"Based on your symptoms, I suspect you might have endometriosis. For the pain, I'm going to prescribe you mefenamic acid 500 milligrams three times a day with food. Take it when you have pain, especially around your period. I'm also going to start you on norethisterone 5 milligrams twice a day to help regulate your cycle and reduce the pain. Take it morning and evening. I'm going to refer you to the gynaecology clinic for further assessment, but there's a waiting list so it might be a few months. In the meantime, I want you to have an ultrasound scan done. Book that with reception, try to get it done in the next month. Come back to see me in six weeks to see how the medication is working. If the pain becomes severe or if you develop any unusual bleeding, contact me straight away. If you get any stomach problems with the mefenamic acid, stop taking it and let me know."

**Expected Extraction:**
- **Medication 1:** Mefenamic acid, 500mg, three times a day, with food, when you have pain
- **Medication 2:** Norethisterone, 5mg, twice a day (morning and evening)
- **Test:** Referral to gynaecology clinic, not urgent, few months wait
- **Test:** Ultrasound scan, next month, book with reception
- **Follow-up:** Come back in six weeks
- **Safety Warning:** "If the pain becomes severe or if you develop any unusual bleeding, contact me straight away"
- **Safety Warning:** "If you get any stomach problems with the mefenamic acid, stop taking it and let me know"

---

## Transcript 9: Mental Health Medication

**Scenario:** Patient starting antidepressant medication

**Transcript:"
"I'm going to start you on citalopram 20 milligrams once a day. Take it in the morning with breakfast. It may take a few weeks to start working, so be patient. You might experience some side effects initially like nausea or headaches, but these usually settle down. If you have any thoughts of self-harm or if you feel worse, contact me straight away or go to A&E. I want to see you back in two weeks to see how you're getting on. Book that appointment before you leave."

**Expected Extraction:**
- **Medication:** Citalopram, 20mg, once a day (morning), with breakfast
- **Safety Warning:** "If you have any thoughts of self-harm or if you feel worse, contact me straight away or go to A&E"
- **Follow-up:** Come back in two weeks, book appointment

---

## Transcript 10: Chronic Condition Management

**Scenario:** Patient with ongoing condition, medication adjustment

**Transcript:"
"Your blood pressure is still a bit high, so I'm going to increase your amlodipine from 5 milligrams to 10 milligrams once a day. Keep taking it at the same time you do now. I also want you to have an ECG done, that's an electrical tracing of your heart. Book that with reception, it's not urgent but try to get it done in the next month. Come back in eight weeks to check your blood pressure again. If you get any swelling in your ankles or if you feel dizzy, let me know."

**Expected Extraction:**
- **Medication:** Amlodipine, increase to 10mg, once a day (same time)
- **Test:** ECG, not urgent, next month, book with reception
- **Follow-up:** Come back in eight weeks
- **Safety Warning:** "If you get any swelling in your ankles or if you feel dizzy, let me know"

---

## Transcript 11: Simple Pain Relief

**Scenario:** Patient with minor injury, simple pain relief

**Transcript:"
"For the pain, I'm going to give you paracetamol 500 milligrams, take two tablets four times a day. You can take it with or without food. Also take ibuprofen 400 milligrams three times a day with food. Take both for the next five days, then stop. If the pain gets worse or if you develop any other symptoms, come back and see me."

**Expected Extraction:**
- **Medication 1:** Paracetamol, 500mg (2 tablets), four times a day, with or without food, for five days
- **Medication 2:** Ibuprofen, 400mg, three times a day, with food, for five days
- **Safety Warning:** "If the pain gets worse or if you develop any other symptoms, come back and see me"

---

## Transcript 12: Complex Multi-Issue Consultation

**Scenario:** Patient with multiple issues requiring various interventions

**Transcript:"
"Right, let's address a few things. First, for your infection, I'm prescribing co-amoxiclav 625 milligrams three times a day for ten days. Take it with food. Second, I'm referring you to the dermatology clinic for that rash, but that's not urgent, probably a few months wait. Third, I want you to have some blood tests done, including a full blood count and liver function tests. Book those with reception, try to get them done in the next week. Fourth, I'm going to start you on propranolol 40 milligrams twice a day for your anxiety. Take it morning and evening. If you feel very tired or if you get any breathing problems, reduce it to once a day and let me know. Come back in three weeks to review everything. If you develop any severe side effects from the antibiotics, like a severe rash or difficulty breathing, stop taking them and contact me immediately or go to A&E."

**Expected Extraction:**
- **Medication 1:** Co-amoxiclav, 625mg, three times a day, with food, for ten days
- **Medication 2:** Propranolol, 40mg, twice a day (morning and evening)
- **Test:** Referral to dermatology clinic, not urgent, few months wait
- **Test:** Blood tests (full blood count, liver function), next week, book with reception
- **Follow-up:** Come back in three weeks
- **Safety Warning:** "If you feel very tired or if you get any breathing problems, reduce it to once a day and let me know"
- **Safety Warning:** "If you develop any severe side effects from the antibiotics, like a severe rash or difficulty breathing, stop taking them and contact me immediately or go to A&E"

---


## Testing Checklist

For each transcript I will verify:

- [ ] **Medications extracted correctly**
  - Medicine name ✓
  - Dosage ✓
  - Frequency ✓
  - Duration ✓
  - Special instructions ✓

- [ ] **Tests/Referrals extracted correctly**
  - Test type ✓
  - Urgency (if stated) ✓
  - Reason (if stated) ✓

- [ ] **Follow-ups extracted correctly**
  - Timeframe ✓
  - Location/method ✓

- [ ] **Safety warnings extracted correctly**
  - Warning text ✓
  - Verbatim quote stored ✓

- [ ] **Additional notes captured**
  - Lifestyle advice ✓
  - Reassurance statements ✓

---

## Notes on Testing

 **Test edge cases**: 
   - Transcripts with no medications
   - Transcripts with partial information
   - Transcripts with multiple medications in one sentence
   - Transcripts with ambiguous instructions

 **Measure accuracy**: For each transcript, manually create "ground truth" extraction, then compare with system output.

 **Test with variations**: Try slight variations in wording to see if extraction is robust.

---

*These transcripts are synthetic and based on common GP consultation patterns. They are designed to test the extraction engine's ability to identify medications, tests, follow-ups, and safety warnings as described in the Calgary-Cambridge model stages 4-5.*


future scrips:
MEDIQA-Chat dataset
Public medical dialogue dataset
Used in clinical note generation research
May require registration/approval
MTS-Dialog dataset
Medical dialogue dataset
Check licensing for research use