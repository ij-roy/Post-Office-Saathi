# Firebase Analytics Event Specification

Generated from the current Android source after the analytics implementation pass. Line numbers refer to the current files in this branch.

## Audit Outcome

Events added:
`forms_index_loaded`, `form_open_failed`, `form_share_failed`, `storage_permission_result`, `capture_prepare_failed`, `corner_detection_failed`, `image_adjust_failed`, `image_rotate_failed`, `pdf_open_failed`, `pdf_share_failed`, `onboarding_page_viewed`, `calculator_input_changed`, `calculation_succeeded`, `calculation_failed`, `result_share_failed`, `agent_contact_failed`, `plan_suggest_failed`, `recent_work_opened`, `recent_work_open_failed`, `recent_work_shared`, `recent_work_share_failed`.

Events removed:
`settings_opened`, `help_opened`, `privacy_opened`, `scheme_selected`, `td_tenure_changed`, `calculation_performed`, `rd_rebate_opened`, `pmi_opened`, and hidden generated `*_failed` events from `recordError(...)`.

Duplicates fixed:
`pdf_layout_selected` now fires once from `PostOfficeSaathiApp`; image rotation no longer logs both UI and ViewModel button taps; PDF save/open/share and form open/share failures are explicit rather than duplicated by `recordError(...)`; settings/help/privacy screen opens rely on `screen_viewed`.

Parameters added:
`search_text`, `result_count`, `customer_name`, `requested_pdf_filename`, `pdf_filename`, `pincode`, `agent_pincode`, `entry_point`, `action_type`, `share_channel`, `sync_result`, `document_name`, `newly_saved`, `item_type`, `page_index`, `page_title`, exact calculator input/result fields such as `monthly_deposit`, `deposit_amount`, `yearly_deposit`, `balance_amount`, `principal_amount`, `interest_rate`, `installments_paid`, `years_completed`, `custom_years`, `compound_frequency`, `scss_extended`, `total_deposited`, `interest_earned`, `maturity_amount`, `total_received`, `monthly_income`, and `maturity_date`.

Analytics locations discovered:
`FirebaseSaathiAnalytics`, `PostOfficeSaathiApp`, `AppSettingsViewModel`, `FormsViewModel`, `PdfCaptureScreens`, `PdfCorrectionScreens`, `PdfPreviewScreens`, `PdfFlowViewModel`, `PdfNameSuccessScreens`, `CalculatorHomeViewModel`, `SchemeCalculatorViewModel`, `SuggestViewModel`, and `RatesSyncWorker`.

Code files modified:
`AnalyticsEvent.kt`, `AnalyticsParam.kt`, `AnalyticsSanitizer.kt`, `FirebaseSaathiAnalytics.kt`, `RatesSyncWorker.kt`, `PostOfficeSaathiApp.kt`, calculator screens/ViewModels/analytics helpers, forms screens/ViewModel/analytics helpers, PDF screens/ViewModel/analytics helpers, onboarding/settings screens/ViewModel, and analytics unit tests.

---

# Event: screen_viewed

## Purpose
Records that a Compose route or modal screen became visible.

## Trigger
`TrackScreen(...)` enters composition for onboarding, home, forms, settings, help, privacy, calculator, scheme calculator, result, placeholder agent/plan routes, PDF layout/capture/preview/name/success, and plan suggester.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/analytics/FirebaseSaathiAnalytics.kt`
- Class: `FirebaseSaathiAnalytics`
- Function: `logScreenViewed`
- Line number: 27

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `screen` | String | `home` | Current screen name from `AnalyticsScreen`. |

## Frequency
Once per screen composition. Recomposition does not relog unless the route leaves and re-enters.

## User Meaning
The user reached that app surface.

## Analytics Value
Answers which screens users reach and where flows drop off.

## Caveats
This is lifecycle telemetry, not a click.

---

# Event: screen_time

## Purpose
Records approximate time spent on a tracked screen.

## Trigger
`TrackScreen(...)` leaves composition and calls `logScreenTime(...)`.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/analytics/FirebaseSaathiAnalytics.kt`
- Class: `FirebaseSaathiAnalytics`
- Function: `logScreenTime`
- Line number: 31

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `screen` | String | `forms` | Screen name. |
| `duration_bucket` | String | `15_60s` | Bucketed elapsed time; the only remaining sanitizer use. |

## Frequency
Once when each tracked screen is disposed.

## User Meaning
The user spent time on that surface.

## Analytics Value
Helps identify high-engagement or confusing screens.

## Caveats
Duration is bucketed and depends on Compose lifecycle.

---

# Event: button_tapped

## Purpose
Records generic low-level button/navigation/control taps when there is no more specific event for the same action.

## Trigger
Calls to `analytics.logButtonTap(...)` from navigation/back/settings/PDF adjustment controls.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/analytics/FirebaseSaathiAnalytics.kt`
- Class: `FirebaseSaathiAnalytics`
- Function: `logButtonTap`
- Line number: 15

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `button_id` | String | `home_download_forms` | Stable control identifier. |
| `screen` | String | `home` | Screen where the tap happened. |

## Frequency
Every eligible tap.

## User Meaning
The user interacted with a clickable control.

## Analytics Value
Shows navigation/control usage where a dedicated event would add no extra meaning.

## Caveats
Specific business actions use dedicated events instead.

---

# Event: storage_permission_result

## Purpose
Records the Android legacy storage permission outcome.

## Trigger
The storage permission launcher returns for legacy file-saving flows.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/PostOfficeSaathiApp.kt`
- Class: `PostOfficeSaathiApp`
- Function: permission launcher callback
- Line number: 121

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | Pending flow that needed permission. |
| `granted` | Boolean | `true` | Whether permission was granted. |

## Frequency
Only when legacy storage permission is requested.

## User Meaning
User allowed or denied file-saving permission.

## Analytics Value
Explains save-flow drop-off on older Android versions.

## Caveats
Modern Android versions skip this permission and do not log the event.

---

# Event: onboarding_page_viewed

## Purpose
Records each onboarding page shown.

## Trigger
`OnboardingScreen` page index changes.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/PostOfficeSaathiApp.kt`
- Class: `PostOfficeSaathiApp`
- Function: onboarding route callback
- Line number: 234

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `page_index` | Int | `1` | Zero-based onboarding page index. |
| `page_title` | String | `Create PDFs` | Visible page title. |

## Frequency
Once per page view.

## User Meaning
User saw a specific onboarding message.

## Analytics Value
Identifies onboarding pages reached before completion or skip.

## Caveats
No parameter records swipe versus next-button navigation.

---

# Event: onboarding_completed

## Purpose
Records that onboarding finished.

## Trigger
User taps Get Started on the final onboarding page.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/settings/AppSettingsViewModel.kt`
- Class: `AppSettingsViewModel`
- Function: `completeOnboarding`
- Line number: 45

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `page_index` | Int | `2` | Page index at completion. |
| `page_title` | String | `Find Recent Work` | Page title at completion. |

## Frequency
Once per install/profile unless app data is cleared.

## User Meaning
User completed onboarding.

## Analytics Value
Measures onboarding completion.

## Caveats
Does not record total time in onboarding.

---

# Event: onboarding_skipped

## Purpose
Records that onboarding was skipped.

## Trigger
User taps Skip before the last onboarding page.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/settings/AppSettingsViewModel.kt`
- Class: `AppSettingsViewModel`
- Function: `completeOnboarding`
- Line number: 45

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `page_index` | Int | `0` | Page index where skip happened. |
| `page_title` | String | `Download Forms` | Page title where skip happened. |

## Frequency
Once per install/profile unless app data is cleared.

## User Meaning
User bypassed onboarding.

## Analytics Value
Shows whether onboarding content is skipped early.

## Caveats
Skip is not shown on the final page.

---

# Event: theme_changed

## Purpose
Records theme preference changes.

## Trigger
User selects System, Light, or Dark in Settings.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/settings/AppSettingsViewModel.kt`
- Class: `AppSettingsViewModel`
- Function: `setThemeMode`
- Line number: 58

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `theme_mode` | String | `dark` | Stored theme preference. |

## Frequency
Every theme selection.

## User Meaning
User changed appearance preference.

## Analytics Value
Measures theme preference.

## Caveats
Selecting the already-active value still logs because the UI invokes the setter.

---

# Event: feedback_email_tapped

## Purpose
Records feedback intent from settings.

## Trigger
User taps Send feedback.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/settings/AppSettingsViewModel.kt`
- Class: `AppSettingsViewModel`
- Function: `logFeedbackEmailTapped`
- Line number: 76

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| none | - | - | No parameters. |

## Frequency
Every feedback tap.

## User Meaning
User attempted to send app feedback.

## Analytics Value
Shows support/contact intent.

## Caveats
Does not confirm email app launch or send completion.

---

# Event: rate_app_tapped

## Purpose
Records app-rating intent.

## Trigger
User taps Rate this app.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/settings/AppSettingsViewModel.kt`
- Class: `AppSettingsViewModel`
- Function: `logRateAppTapped`
- Line number: 80

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| none | - | - | No parameters. |

## Frequency
Every rate-app tap.

## User Meaning
User showed rating intent.

## Analytics Value
Measures rating CTA engagement.

## Caveats
Does not confirm Play Store opened successfully.

---

# Event: review_prompt_requested

## Purpose
Records when the app requests an in-app review prompt after meaningful actions.

## Trigger
`recordMeaningfulAction(...)` policy allows a review request.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/settings/AppSettingsViewModel.kt`
- Class: `AppSettingsViewModel`
- Function: `recordMeaningfulAction`
- Line number: 69

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| none | - | - | No parameters. |

## Frequency
Only when the review policy threshold is met.

## User Meaning
The user completed enough meaningful work to request a review.

## Analytics Value
Tracks review prompt eligibility.

## Caveats
Does not confirm the Play review UI appeared.

---

# Event: recent_work_opened

## Purpose
Records successful opening of a recent PDF/form.

## Trigger
Home recent-work open external intent launches successfully.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/PostOfficeSaathiApp.kt`
- Class: `PostOfficeSaathiApp`
- Function: home external action collector
- Line number: 162

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `screen` | String | `home` | Source screen. |
| `item_type` | String | `CreatedPdf` | Recent work type. |
| `document_name` | String | `Sita_Document_2026-07-02.pdf` | Exact displayed file name. |

## Frequency
Every successful recent-work open.

## User Meaning
User reused a saved file from Home.

## Analytics Value
Shows value of the Recent Work panel.

## Caveats
Intent success means Android accepted the open request, not that the target app displayed the file.

---

# Event: recent_work_open_failed

## Purpose
Records failed recent-work open attempts.

## Trigger
Home recent-work open intent throws.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/PostOfficeSaathiApp.kt`
- Class: `PostOfficeSaathiApp`
- Function: home external action collector
- Line number: 184

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `screen` | String | `home` | Source screen. |
| `item_type` | String | `Form` | Recent work type. |
| `document_name` | String | `sb3.pdf` | Exact displayed file name. |
| `error_area` | String | `recent_work_open` | Failure area. |
| `error_type` | String | `ActivityNotFoundException` | Throwable class. |

## Frequency
Only on failure.

## User Meaning
User tried to open recent work but Android could not handle it.

## Analytics Value
Identifies external PDF/opening reliability issues.

## Caveats
Crashlytics also records the exception.

---

# Event: recent_work_shared

## Purpose
Records successful sharing of a recent PDF/form.

## Trigger
Home recent-work share intent launches successfully.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/PostOfficeSaathiApp.kt`
- Class: `PostOfficeSaathiApp`
- Function: home external action collector
- Line number: 166

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `screen` | String | `home` | Source screen. |
| `item_type` | String | `CreatedPdf` | Recent work type. |
| `document_name` | String | `Sita_Document_2026-07-02.pdf` | Exact displayed file name. |

## Frequency
Every successful recent-work share.

## User Meaning
User shared a saved file from Home.

## Analytics Value
Shows reuse/sharing value of saved outputs.

## Caveats
Does not confirm recipient selection or send completion.

---

# Event: recent_work_share_failed

## Purpose
Records failed recent-work share attempts.

## Trigger
Home recent-work share intent throws.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/PostOfficeSaathiApp.kt`
- Class: `PostOfficeSaathiApp`
- Function: home external action collector
- Line number: 186

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `screen` | String | `home` | Source screen. |
| `item_type` | String | `Form` | Recent work type. |
| `document_name` | String | `sb3.pdf` | Exact displayed file name. |
| `error_area` | String | `recent_work_share` | Failure area. |
| `error_type` | String | `ActivityNotFoundException` | Throwable class. |

## Frequency
Only on failure.

## User Meaning
User tried to share recent work but Android could not handle it.

## Analytics Value
Identifies external share reliability issues.

## Caveats
Crashlytics also records the exception.

---

# Event: forms_index_loaded

## Purpose
Records form index loading.

## Trigger
`FormsViewModel.loadForms()` completes repository loading.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/forms/FormsViewModel.kt`
- Class: `FormsViewModel`
- Function: `loadForms`
- Line number: 67

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `forms` | Forms flow. |
| `from_cache` | Boolean | `false` | Whether cached forms index was used. |
| `result_count` | Int | `42` | Exact number of forms loaded. |

## Frequency
Every FormsViewModel load.

## User Meaning
The forms catalog became available or fell back to cache.

## Analytics Value
Shows catalog availability and cache reliance.

## Caveats
Repository failure with no cache is represented by zero results and message state, not a separate event.

---

# Event: form_search

## Purpose
Records every Forms search state exactly.

## Trigger
`FormsViewModel.updateQuery(...)` runs for every query value from the search text field.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/forms/FormsViewModel.kt`
- Class: `FormsViewModel`
- Function: `logSearch`
- Line number: 198

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `forms` | Forms flow. |
| `search_text` | String | `Post Of` | Exact query text entered by the user. |
| `result_count` | Int | `6` | Exact visible result count. |

## Frequency
Every query update, including blank/clear states.

## User Meaning
User searched or changed a search query.

## Analytics Value
Shows demand for form names/keywords and live search behavior.

## Caveats
No sanitization or truncation is applied by app code.

---

# Event: form_search_empty

## Purpose
Records exact nonblank Forms searches with no matches.

## Trigger
`FormsViewModel.logSearch(...)` sees nonblank `search_text` and `result_count == 0`.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/forms/FormsViewModel.kt`
- Class: `FormsViewModel`
- Function: `logSearch`
- Line number: 200

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `forms` | Forms flow. |
| `search_text` | String | `Passport` | Exact failed search. |
| `result_count` | Int | `0` | No matches. |

## Frequency
Every nonblank no-result query update.

## User Meaning
User searched for a form but no form matched.

## Analytics Value
Finds missing forms or search keyword gaps.

## Caveats
Can fire repeatedly while typing a no-result query.

---

# Event: form_download_started

## Purpose
Records start of a form download/preparation action.

## Trigger
User taps form Open/Download or Share, before repository download/cache lookup.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/forms/FormsViewModel.kt`
- Class: `FormsViewModel`
- Function: `openForm` / `shareForm`
- Line number: 98, 124

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `forms` | Forms flow. |
| `form_id` | String | `sb3` | Form identifier. |
| `form_category` | String | `Savings` | Form category. |
| `form_language` | String | `English` | Form language. |
| `search_text` | String | `Account` | Exact active query when tapped. |
| `action_type` | String | `open` | `open` or `share`. |

## Frequency
Every form open/download/share attempt.

## User Meaning
User asked for a specific form.

## Analytics Value
Identifies form demand and action intent.

## Caveats
Start does not mean the file was downloaded successfully.

---

# Event: form_download_succeeded

## Purpose
Records successful form download/cache retrieval.

## Trigger
`repository.downloadForm(form)` succeeds.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/forms/FormsViewModel.kt`
- Class: `FormsViewModel`
- Function: `openForm` / `shareForm`
- Line number: 101, 127

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `forms` | Forms flow. |
| `form_id` | String | `sb3` | Form identifier. |
| `form_category` | String | `Savings` | Form category. |
| `form_language` | String | `English` | Form language. |
| `search_text` | String | `Account` | Exact active query. |
| `action_type` | String | `share` | Requested action. |
| `document_name` | String | `sb3.pdf` | Exact local/display name. |
| `newly_saved` | Boolean | `true` | Whether this run saved a new file. |

## Frequency
Only on successful repository result.

## User Meaning
The requested form became locally available.

## Analytics Value
Measures form availability and cache reuse.

## Caveats
External open/share can still fail after this event.

---

# Event: form_download_failed

## Purpose
Records failed form download/cache retrieval.

## Trigger
`repository.downloadForm(form)` throws.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/forms/FormsViewModel.kt`
- Class: `FormsViewModel`
- Function: `openForm` / `shareForm`
- Line number: 112, 138

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `forms` | Forms flow. |
| `form_id` | String | `sb3` | Form identifier. |
| `search_text` | String | `Account` | Exact active query. |
| `action_type` | String | `open` | Requested action. |
| `error_area` | String | `form_download` | Failure area. |
| `error_type` | String | `OfflineFormsException` | Throwable class. |

## Frequency
Only on failure.

## User Meaning
User requested a form but the app could not prepare it.

## Analytics Value
Shows offline/network/storage failures by form.

## Caveats
Crashlytics also records the exception.

---

# Event: form_opened

## Purpose
Records successful external open of a form PDF.

## Trigger
Forms route launches the open intent successfully.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/forms/FormsViewModel.kt`
- Class: `FormsViewModel`
- Function: `onFormOpened`
- Line number: 146

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `forms` | Forms flow. |
| `form_id` | String | `sb3` | Form identifier. |
| `search_text` | String | `Account` | Exact active query. |
| `action_type` | String | `open` | Open action. |
| `document_name` | String | `sb3.pdf` | Exact displayed name. |
| `newly_saved` | Boolean | `false` | Whether file was newly saved. |

## Frequency
Every successful external open request.

## User Meaning
User opened a form.

## Analytics Value
Shows which downloaded forms are actually opened.

## Caveats
Android accepting the intent does not prove the user read the form.

---

# Event: form_open_failed

## Purpose
Records failed external open of a form PDF.

## Trigger
Forms route catches an open intent failure.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/forms/FormsViewModel.kt`
- Class: `FormsViewModel`
- Function: `onExternalActionFailed`
- Line number: 166

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `forms` | Forms flow. |
| `form_id` | String | `sb3` | Form identifier. |
| `search_text` | String | `Account` | Exact active query. |
| `action_type` | String | `open` | Open action. |
| `error_area` | String | `form_open` | Failure area. |
| `error_type` | String | `ActivityNotFoundException` | Throwable class. |

## Frequency
Only on failure.

## User Meaning
User tried to open a prepared form but Android could not open it.

## Analytics Value
Identifies device/app compatibility gaps.

## Caveats
Crashlytics also records the exception.

---

# Event: form_shared

## Purpose
Records successful external share of a form PDF.

## Trigger
Forms route launches the share intent successfully.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/forms/FormsViewModel.kt`
- Class: `FormsViewModel`
- Function: `onFormShared`
- Line number: 150

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `forms` | Forms flow. |
| `form_id` | String | `sb3` | Form identifier. |
| `search_text` | String | `Account` | Exact active query. |
| `action_type` | String | `share` | Share action. |
| `document_name` | String | `sb3.pdf` | Exact displayed name. |
| `newly_saved` | Boolean | `true` | Whether file was newly saved. |

## Frequency
Every successful external share request.

## User Meaning
User shared or attempted to share a form.

## Analytics Value
Shows which forms are useful enough to share.

## Caveats
Does not prove recipient selection or message send.

---

# Event: form_share_failed

## Purpose
Records failed external share of a form PDF.

## Trigger
Forms route catches a share intent failure.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/forms/FormsViewModel.kt`
- Class: `FormsViewModel`
- Function: `onExternalActionFailed`
- Line number: 166

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `forms` | Forms flow. |
| `form_id` | String | `sb3` | Form identifier. |
| `search_text` | String | `Account` | Exact active query. |
| `action_type` | String | `share` | Share action. |
| `error_area` | String | `form_share` | Failure area. |
| `error_type` | String | `ActivityNotFoundException` | Throwable class. |

## Frequency
Only on failure.

## User Meaning
User tried to share a prepared form but Android could not handle it.

## Analytics Value
Shows share reliability issues.

## Caveats
Crashlytics also records the exception.

---

# Event: pdf_flow_started

## Purpose
Records entry into PDF creation.

## Trigger
User taps Create PDF from Home after storage permission gate passes or is not needed.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/PostOfficeSaathiApp.kt`
- Class: `PostOfficeSaathiApp`
- Function: Home route callback
- Line number: 255

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |

## Frequency
Every PDF flow entry.

## User Meaning
User chose to create a PDF.

## Analytics Value
Measures demand for PDF creation.

## Caveats
Does not mean a layout was selected or a PDF was saved.

---

# Event: pdf_layout_selected

## Purpose
Records selected PDF layout.

## Trigger
User selects 1, 2, or 3 card layout.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/PostOfficeSaathiApp.kt`
- Class: `PostOfficeSaathiApp`
- Function: PDF layout route callback
- Line number: 432

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `two_documents` | Selected layout. |

## Frequency
Once per layout selection.

## User Meaning
User chose the final PDF card count/layout.

## Analytics Value
Shows which PDF layout is preferred.

## Caveats
Now logs once; previous duplicate ViewModel event was removed.

---

# Event: camera_permission_result

## Purpose
Records camera permission grant/deny.

## Trigger
Camera permission launcher returns in the capture screen.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfCaptureScreens.kt`
- Class: `DocumentCaptureScreen`
- Function: permission launcher callback
- Line number: 183

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `one_document` | Current layout. |
| `granted` | Boolean | `true` | Permission outcome. |

## Frequency
Only when camera permission is requested.

## User Meaning
User allowed or denied camera access.

## Analytics Value
Explains capture-flow drop-off.

## Caveats
Already-granted permission does not log.

---

# Event: capture_started

## Purpose
Records camera capture attempt.

## Trigger
User taps the camera shutter.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfCaptureScreens.kt`
- Class: `DocumentCaptureScreen`
- Function: camera capture callback
- Line number: 337

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `three_cards` | Current layout. |
| `capture_source` | String | `camera` | Capture source. |

## Frequency
Every camera capture attempt.

## User Meaning
User tried to take a photo for the PDF.

## Analytics Value
Measures capture usage and attempts.

## Caveats
Does not mean the image was saved or accepted.

---

# Event: capture_succeeded

## Purpose
Records that all required PDF images were captured/imported and adjusted.

## Trigger
User applies the last corrected image for the selected layout.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfCaptureScreens.kt`
- Class: `DocumentCaptureScreen`
- Function: correction apply callback
- Line number: 239

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `two_documents` | Current layout. |
| `image_count` | Int | `2` | Number of accepted images. |

## Frequency
Once per completed capture set.

## User Meaning
User completed image capture/import for a PDF.

## Analytics Value
Shows capture-flow completion by layout.

## Caveats
PDF may still be edited or fail during creation.

---

# Event: capture_failed

## Purpose
Records failed camera capture.

## Trigger
CameraX `ImageCapture` returns an error.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfCaptureScreens.kt`
- Class: `DocumentCaptureScreen`
- Function: `capturePhoto` error callback
- Line number: 359

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `one_document` | Current layout. |
| `capture_source` | String | `camera` | Source that failed. |
| `error_area` | String | `capture` | Failure area. |
| `error_type` | String | `ImageCaptureException` | Throwable class. |

## Frequency
Only on camera capture failure.

## User Meaning
User tried to capture but the camera pipeline failed.

## Analytics Value
Monitors capture reliability.

## Caveats
Crashlytics also records the exception.

---

# Event: capture_prepare_failed

## Purpose
Records failure while preparing a captured photo.

## Trigger
`PdfFlowViewModel.prepareCapturedPhoto(...)` catches image rewrite failure.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfFlowViewModel.kt`
- Class: `PdfFlowViewModel`
- Function: `prepareCapturedPhoto`
- Line number: 127

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `one_document` | Current layout. |
| `error_area` | String | `capture_prepare` | Failure area. |
| `error_type` | String | `IOException` | Throwable class. |

## Frequency
Only on preparation failure.

## User Meaning
The app could not normalize a captured image, but continues with original file.

## Analytics Value
Shows image processing reliability.

## Caveats
The flow may continue despite this failure.

---

# Event: gallery_import_succeeded

## Purpose
Records successful gallery image import.

## Trigger
Gallery picker returns a URI and `copyGalleryImageToCache(...)` succeeds.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfCaptureScreens.kt`
- Class: `DocumentCaptureScreen`
- Function: gallery launcher callback
- Line number: 193

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `two_documents` | Current layout. |
| `capture_source` | String | `gallery` | Import source. |

## Frequency
Every successful gallery import.

## User Meaning
User imported a PDF image from gallery.

## Analytics Value
Compares camera versus gallery use.

## Caveats
Image can still fail later during adjustment or PDF creation.

---

# Event: gallery_import_failed

## Purpose
Records failed gallery import.

## Trigger
`PdfFlowViewModel.importGalleryImage(...)` catches copy/decode failure.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfFlowViewModel.kt`
- Class: `PdfFlowViewModel`
- Function: `importGalleryImage`
- Line number: 110

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `two_documents` | Current layout. |
| `error_area` | String | `gallery_import` | Failure area. |
| `error_type` | String | `IOException` | Throwable class. |

## Frequency
Only on import failure.

## User Meaning
User chose an image the app could not import.

## Analytics Value
Shows file picker/import reliability.

## Caveats
Crashlytics also records the exception.

---

# Event: corner_detection_result

## Purpose
Records automatic corner detection result.

## Trigger
`PdfFlowViewModel.loadCorners(...)` completes successfully.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfFlowViewModel.kt`
- Class: `PdfFlowViewModel`
- Function: `loadCorners`
- Line number: 150

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `one_document` | Current layout. |
| `used_fallback` | Boolean | `true` | Whether default corners were used. |

## Frequency
Every image entering corner detection.

## User Meaning
The app detected or defaulted document corners.

## Analytics Value
Measures corner detection quality.

## Caveats
Default corners can be a valid fallback, not necessarily fatal.

---

# Event: corner_detection_failed

## Purpose
Records failed corner detection.

## Trigger
`PdfFlowViewModel.loadCorners(...)` catches detection failure.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfFlowViewModel.kt`
- Class: `PdfFlowViewModel`
- Function: `loadCorners`
- Line number: 156

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `one_document` | Current layout. |
| `error_area` | String | `corner_detection` | Failure area. |
| `error_type` | String | `CvException` | Throwable class. |

## Frequency
Only on detection failure.

## User Meaning
The app failed to detect corners and used defaults.

## Analytics Value
Shows OpenCV/detection reliability.

## Caveats
The flow may continue with default corners.

---

# Event: image_adjusted

## Purpose
Records successful creation of a corrected card image.

## Trigger
User taps Apply and `createCorrectedCardImage(...)` succeeds.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfFlowViewModel.kt`
- Class: `PdfFlowViewModel`
- Function: `createCorrectedImage`
- Line number: 194

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `three_cards` | Current layout. |

## Frequency
Every successfully adjusted image.

## User Meaning
User accepted corner adjustment for an image.

## Analytics Value
Measures progression through correction.

## Caveats
No image index is logged.

---

# Event: image_adjust_failed

## Purpose
Records failed image correction.

## Trigger
`PdfFlowViewModel.createCorrectedImage(...)` catches correction failure.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfFlowViewModel.kt`
- Class: `PdfFlowViewModel`
- Function: `createCorrectedImage`
- Line number: 199

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `three_cards` | Current layout. |
| `error_area` | String | `image_adjust` | Failure area. |
| `error_type` | String | `IOException` | Throwable class. |

## Frequency
Only on adjustment failure.

## User Meaning
User tried to apply correction but processing failed.

## Analytics Value
Monitors image processing stability.

## Caveats
Crashlytics also records the exception.

---

# Event: image_rotate_failed

## Purpose
Records failed image rotation.

## Trigger
`PdfFlowViewModel.rotateWorkingImage(...)` catches rotation failure.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfFlowViewModel.kt`
- Class: `PdfFlowViewModel`
- Function: `rotateWorkingImage`
- Line number: 179

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `one_document` | Current layout. |
| `error_area` | String | `image_rotate` | Failure area. |
| `error_type` | String | `IOException` | Throwable class. |

## Frequency
Only on rotation failure.

## User Meaning
User tried to rotate an image but processing failed.

## Analytics Value
Tracks correction-tool reliability.

## Caveats
Successful rotation is represented by the `button_tapped` rotate control event, not a dedicated success event.

---

# Event: pdf_preview_reset

## Purpose
Records preview layout reset.

## Trigger
User confirms Reset in PDF Preview.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfPreviewScreens.kt`
- Class: `PdfPreviewEditorScreen`
- Function: reset confirmation
- Line number: 177

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `two_documents` | Current layout. |

## Frequency
Every reset confirmation.

## User Meaning
User discarded manual placement edits.

## Analytics Value
Shows whether preview editing is confusing or needed.

## Caveats
Canceling reset is not logged.

---

# Event: pdf_create_started

## Purpose
Records PDF save start with exact naming inputs.

## Trigger
User taps Save PDF in the name screen.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfFlowViewModel.kt`
- Class: `PdfFlowViewModel`
- Function: `createPdf`
- Line number: 210

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `two_documents` | Selected layout. |
| `customer_name` | String | `Sita Devi` | Exact text from name field. |
| `requested_pdf_filename` | String | `Sita_Devi_Two_Documents_2026-07-02.pdf` | Filename generated before collision handling. |
| `image_count` | Int | `2` | Images in the PDF. |

## Frequency
Every Save PDF attempt.

## User Meaning
User attempted to create a named PDF.

## Analytics Value
Shows PDF save attempts and naming patterns.

## Caveats
The final stored filename can differ if storage adds a suffix.

---

# Event: pdf_create_succeeded

## Purpose
Records successful PDF creation with final stored filename.

## Trigger
`PdfCreationUseCase.createPdf(...)` succeeds.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfFlowViewModel.kt`
- Class: `PdfFlowViewModel`
- Function: `createPdf`
- Line number: 230

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `two_documents` | Selected layout. |
| `customer_name` | String | `Sita Devi` | Exact text from name field. |
| `requested_pdf_filename` | String | `Sita_Devi_Two_Documents_2026-07-02.pdf` | Requested filename. |
| `pdf_filename` | String | `Sita_Devi_Two_Documents_2026-07-02-2.pdf` | Final display name returned by storage. |
| `image_count` | Int | `2` | Images in the PDF. |

## Frequency
Only on successful PDF creation.

## User Meaning
User created a PDF.

## Analytics Value
Measures successful PDF output and layout/name usage.

## Caveats
No PDF content or image content is logged.

---

# Event: pdf_create_failed

## Purpose
Records failed PDF creation.

## Trigger
`PdfCreationUseCase.createPdf(...)` throws.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfFlowViewModel.kt`
- Class: `PdfFlowViewModel`
- Function: `createPdf`
- Line number: 257

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `layout_type` | String | `two_documents` | Selected layout. |
| `customer_name` | String | `Sita Devi` | Exact text from name field. |
| `requested_pdf_filename` | String | `Sita_Devi_Two_Documents_2026-07-02.pdf` | Requested filename. |
| `image_count` | Int | `2` | Images in the PDF. |
| `error_area` | String | `pdf_create` | Failure area. |
| `error_type` | String | `IOException` | Throwable class. |

## Frequency
Only on PDF creation failure.

## User Meaning
User tried to save a PDF but the app failed.

## Analytics Value
Shows PDF generation/storage reliability.

## Caveats
Crashlytics also records the exception.

---

# Event: pdf_opened

## Purpose
Records successful external open of a created PDF.

## Trigger
User taps Open on PDF success screen and Android accepts the open intent.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfNameSuccessScreens.kt`
- Class: `PdfCreatedSuccessScreen`
- Function: Open click handler
- Line number: 267

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `pdf_filename` | String | `Sita_Document_2026-07-02.pdf` | Final displayed filename. |

## Frequency
Every successful open intent.

## User Meaning
User opened the created PDF.

## Analytics Value
Shows whether users inspect PDFs after creation.

## Caveats
Intent success does not prove the PDF rendered.

---

# Event: pdf_open_failed

## Purpose
Records failed external open of a created PDF.

## Trigger
Open intent throws on PDF success screen.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfNameSuccessScreens.kt`
- Class: `PdfCreatedSuccessScreen`
- Function: Open click handler
- Line number: 281

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `pdf_filename` | String | `Sita_Document_2026-07-02.pdf` | Final displayed filename. |
| `error_area` | String | `pdf_open` | Failure area. |
| `error_type` | String | `ActivityNotFoundException` | Throwable class. |

## Frequency
Only on failure.

## User Meaning
User tried to open a PDF but Android could not handle it.

## Analytics Value
Identifies PDF viewer availability issues.

## Caveats
Crashlytics also records the exception.

---

# Event: pdf_shared

## Purpose
Records successful external share of a created PDF.

## Trigger
User taps Share on PDF success screen and Android accepts share intent.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfNameSuccessScreens.kt`
- Class: `PdfCreatedSuccessScreen`
- Function: Share click handler
- Line number: 295

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `pdf_filename` | String | `Sita_Document_2026-07-02.pdf` | Final displayed filename. |

## Frequency
Every successful share intent.

## User Meaning
User shared or attempted to share the created PDF.

## Analytics Value
Measures PDF share value.

## Caveats
Does not confirm recipient selection or send completion.

---

# Event: pdf_share_failed

## Purpose
Records failed external share of a created PDF.

## Trigger
Share intent throws on PDF success screen.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/pdf/PdfNameSuccessScreens.kt`
- Class: `PdfCreatedSuccessScreen`
- Function: Share click handler
- Line number: 309

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `pdf` | PDF flow. |
| `pdf_filename` | String | `Sita_Document_2026-07-02.pdf` | Final displayed filename. |
| `error_area` | String | `pdf_share` | Failure area. |
| `error_type` | String | `ActivityNotFoundException` | Throwable class. |

## Frequency
Only on failure.

## User Meaning
User tried to share a PDF but Android could not handle it.

## Analytics Value
Identifies share reliability issues.

## Caveats
Crashlytics also records the exception.

---

# Event: calculator_opened

## Purpose
Records opening calculator home and individual scheme calculators.

## Trigger
Calculator home cards load, or scheme calculator rates/custom state load.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/CalculatorHomeViewModel.kt`
- Class: `CalculatorHomeViewModel`
- Function: `loadCards`
- Line number: 85
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/scheme/SchemeCalculatorViewModel.kt`
- Class: `SchemeCalculatorViewModel`
- Function: `logOpened`
- Line number: 330

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `calculator` | Calculator flow. |
| `entry_point` | String | `calculator_home` | `home`, `calculator_home`, or `plan_suggester`. |
| `scheme_type` | String | `RD` | Present for scheme calculator openings. |
| `initial_amount` | Double | `25000.0` | Present when opened from plan suggestions. |
| `rates_version` | String | `2026-07-01` | Rate dataset version when available. |
| `used_fallback` | Boolean | `false` | Whether rate fallback is active when available. |

## Frequency
Once per ViewModel load/open.

## User Meaning
User entered calculator home or a specific calculator.

## Analytics Value
Shows calculator discovery and scheme interest.

## Caveats
Home load can occur when ViewModel is recreated.

---

# Event: calculator_input_changed

## Purpose
Records meaningful discrete calculator input changes.

## Trigger
User changes date, TD tenure, custom type, compound frequency, SCSS extension, or rate override state.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/scheme/SchemeCalculatorViewModel.kt`
- Class: `SchemeCalculatorViewModel`
- Function: `logInputChanged`
- Line number: 344

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `calculator` | Calculator flow. |
| `scheme_type` | String | `TD` | Current scheme. |
| `field_name` | String | `td_tenure` | Changed field. |
| `field_value` | String/Boolean | `3Y` | Exact selected value. |
| `rates_version` | String | `2026-07-01` | Rate dataset version when available. |

## Frequency
Every discrete selector/date/rate-control change.

## User Meaning
User adjusted calculation assumptions.

## Analytics Value
Shows which calculator controls matter.

## Caveats
Text-field keystrokes are intentionally not logged; exact text values are logged on calculation outcome.

---

# Event: calculation_succeeded

## Purpose
Records successful calculator result generation with exact inputs and outputs.

## Trigger
User taps Calculate, validation passes, and `InterestEngine.calculate(...)` succeeds.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/scheme/SchemeCalculatorViewModel.kt`
- Class: `SchemeCalculatorViewModel`
- Function: `calculate`
- Line number: 243

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `calculator` | Calculator flow. |
| `scheme_type` | String | `RD` | Effective scheme calculated. |
| `amount` | Double | `5000.0` | Exact base amount. |
| `monthly_deposit` | Double | `5000.0` | RD/RD rebate amount. |
| `deposit_amount` | Double | `100000.0` | TD/MIS/NSC/KVP/SCSS/MSSC amount. |
| `yearly_deposit` | Double | `50000.0` | PPF/SSY yearly deposit. |
| `balance_amount` | Double | `25000.0` | SB balance. |
| `principal_amount` | Double | `100000.0` | Custom/simple/compound/PMI principal. |
| `interest_rate` | Double | `7.5` | Exact rate used. |
| `start_date` | String | `2026-07-02` | Opening/from date. |
| `to_date` | String | `2026-12-31` | End date when applicable. |
| `td_tenure` | String | `5Y` | TD tenure. |
| `installments_paid` | Int | `36` | RD/RD rebate installments. |
| `years_completed` | Int | `3` | PPF/SSY completed years. |
| `custom_type` | String | `Compound` | Custom calculator mode. |
| `custom_years` | Double | `2.5` | Custom calculator duration. |
| `compound_frequency` | String | `Quarterly` | Custom compound frequency. |
| `compounding_frequency` | String | `QUARTERLY` | Engine compounding mode. |
| `scss_extended` | Boolean | `true` | SCSS extension state. |
| `rates_version` | String | `2026-07-01` | Rate dataset version. |
| `used_fallback` | Boolean | `false` | Whether fallback rate was used. |
| `total_deposited` | Double | `180000.0` | Exact calculated deposited total. |
| `interest_earned` | Double | `20345.0` | Exact calculated interest. |
| `maturity_amount` | Double | `200345.0` | Exact calculated maturity/payable amount. |
| `total_received` | Double | `250000.0` | Exact total received where applicable. |
| `monthly_income` | Double | `625.0` | MIS monthly or SCSS quarterly payout field. |
| `maturity_date` | String | `2031-07-02` | Calculated maturity/end date. |

## Frequency
Every successful calculate press.

## User Meaning
User generated a calculator result.

## Analytics Value
Answers which calculators are used, with what exact assumptions and outputs.

## Caveats
No bucketing, sanitization, or truncation is applied by app code.

---

# Event: calculation_failed

## Purpose
Records failed calculator attempts.

## Trigger
User taps Calculate and validation fails, or calculation throws.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/scheme/SchemeCalculatorViewModel.kt`
- Class: `SchemeCalculatorViewModel`
- Function: `calculate`
- Line number: 210, 237

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `calculator` | Calculator flow. |
| `scheme_type` | String | `MSSC` | Current scheme. |
| `amount` | String | `abc` | Exact entered amount text after UI filtering. |
| `interest_rate` | Double/String | `7.5` | Active or entered rate. |
| `start_date` | String | `2026-07-02` | Current date. |
| `to_date` | String | `2027-07-02` | Current end date. |
| `td_tenure` | String | `5Y` | When TD. |
| `custom_type` | String | `Simple` | When custom. |
| `custom_years` | String | `0` | When custom. |
| `compound_frequency` | String | `Annually` | When compound. |
| `installments_paid` | String | `61` | When RD. |
| `years_completed` | String | `3` | When PPF/SSY. |
| `scss_extended` | Boolean | `false` | When SCSS. |
| `rates_version` | String | `2026-07-01` | Rate dataset version. |
| `used_fallback` | Boolean | `true` | Current fallback state. |
| `error_area` | String | `calculator_validation` | Validation or calculation area. |
| `error_type` | String | `Validation` | Validation or throwable class. |
| `error_fields` | String | `amount,rate` | Validation fields that failed. |

## Frequency
Only on failed calculate press.

## User Meaning
User attempted calculation but inputs or engine failed.

## Analytics Value
Shows which calculators have validation friction or runtime issues.

## Caveats
Validation failures are analytics-only; thrown errors also go to Crashlytics.

---

# Event: calculator_rate_fallback_used

## Purpose
Records when a scheme calculator uses a fallback rate for a requested date.

## Trigger
Successful calculation detects `officialRate.usedFallback == true` and rate override is not active.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/scheme/SchemeCalculatorViewModel.kt`
- Class: `SchemeCalculatorViewModel`
- Function: `logRateFallback`
- Line number: 313

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `scheme_type` | String | `TD` | Rate lookup scheme. |
| `input_year` | Int | `2010` | Requested date year. |
| `input_month` | Int | `5` | Requested date month. |
| `rates_version` | String | `2026-07-01` | Dataset version. |
| `fallback_rate` | Double | `7.5` | Rate used. |
| `fallback_effective_from` | String | `2026-07-01` | Effective date used. |

## Frequency
Only on successful calculations using fallback official rate.

## User Meaning
User selected a date without exact rate history.

## Analytics Value
Identifies rate history gaps by scheme/date.

## Caveats
Does not fire when user manually overrides the rate.

---

# Event: result_shared

## Purpose
Records successful sharing of calculator results.

## Trigger
User taps WhatsApp or More Apps share and Android accepts the share intent.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/PostOfficeSaathiApp.kt`
- Class: `PostOfficeSaathiApp`
- Function: calculator result route share callbacks
- Line number: 367, 383

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `calculator` | Calculator flow. |
| `scheme_type` | String | `MIS` | Result scheme. |
| `share_channel` | String | `whatsapp` | Actual launched channel. |
| `interest_rate` | Double | `7.4` | Result rate. |
| `total_deposited` | Double | `100000.0` | Result deposited amount. |
| `interest_earned` | Double | `37000.0` | Result interest. |
| `maturity_amount` | Double | `100000.0` | Result maturity amount. |
| `total_received` | Double | `137000.0` | Result total received. |
| `monthly_income` | Double | `616.67` | MIS/SCSS payout field when present. |
| `maturity_date` | String | `2031-07-02` | Result maturity date. |

## Frequency
Every successful share intent.

## User Meaning
User shared or attempted to share a calculator result.

## Analytics Value
Shows which results are useful enough to share and via which channel.

## Caveats
Does not prove recipient selection or send completion.

---

# Event: result_share_failed

## Purpose
Records failed calculator result sharing.

## Trigger
Share intent throws.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/PostOfficeSaathiApp.kt`
- Class: `PostOfficeSaathiApp`
- Function: calculator result route share callbacks
- Line number: 372, 388

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| calculator result params | Mixed | see `result_shared` | Same result fields as success. |
| `share_channel` | String | `whatsapp` | Requested channel. |
| `error_area` | String | `result_share` | Failure area. |
| `error_type` | String | `ActivityNotFoundException` | Throwable class. |

## Frequency
Only on share failure.

## User Meaning
User tried to share a calculation but Android could not handle it.

## Analytics Value
Identifies share reliability problems.

## Caveats
Crashlytics also records the exception.

---

# Event: plan_suggested

## Purpose
Records generated plan suggestions.

## Trigger
User taps Suggest with a valid amount and suggestions are calculated.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/suggest/SuggestViewModel.kt`
- Class: `SuggestViewModel`
- Function: `suggestPlans`
- Line number: 109

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `calculator` | Calculator flow. |
| `investment_amount` | Double | `25000.0` | Exact amount entered. |
| `result_count` | Int | `6` | Exact suggestion count. |
| `top_scheme` | String | `KVP` | Highest sorted suggestion. |
| `top_maturity_amount` | Double | `50000.0` | Top suggestion maturity/received amount. |

## Frequency
Every successful suggestion generation.

## User Meaning
User asked the app to compare plans for an amount.

## Analytics Value
Shows plan-suggestion demand and preferred outcomes.

## Caveats
Only schemes listed in the source suggestion list are considered.

---

# Event: plan_suggest_failed

## Purpose
Records failed plan suggestion attempts.

## Trigger
Invalid amount or rates/calculation loading failure.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/suggest/SuggestViewModel.kt`
- Class: `SuggestViewModel`
- Function: `suggestPlans`
- Line number: 71, 104

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `calculator` | Calculator flow. |
| `investment_amount` | String | `0` | Exact amount text or parsed amount string. |
| `error_area` | String | `plan_suggest_validation` | Validation/load area. |
| `error_type` | String | `Validation` | Validation or throwable class. |

## Frequency
Only on failure.

## User Meaning
User attempted plan suggestion but input or loading failed.

## Analytics Value
Shows suggestion friction and reliability.

## Caveats
Thrown errors also go to Crashlytics.

---

# Event: plan_detail_opened

## Purpose
Records opening a suggested plan in its calculator.

## Trigger
User taps Calculate in detail on a suggestion row.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/suggest/SuggestViewModel.kt`
- Class: `SuggestViewModel`
- Function: `logPlanDetailOpened`
- Line number: 160

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `calculator` | Calculator flow. |
| `scheme_type` | String | `TD` | Suggested scheme. |
| `investment_amount` | Double | `25000.0` | Exact amount used in suggestion. |
| `maturity_amount` | Double | `31800.0` | Suggested maturity amount. |

## Frequency
Every suggestion detail tap.

## User Meaning
User wanted details for a suggested scheme.

## Analytics Value
Shows which recommendations convert into detailed calculators.

## Caveats
The destination calculator logs its own `calculator_opened` too.

---

# Event: agent_search_performed

## Purpose
Records Agent Finder search by exact pincode.

## Trigger
User taps Find in the plan suggester agent section, including validation failures.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/suggest/SuggestViewModel.kt`
- Class: `SuggestViewModel`
- Function: `searchAgents`
- Line number: 127, 135

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `calculator` | Calculator flow. |
| `pincode` | String | `125055` | Exact pincode entered by the user. |
| `result_count` | Int | `2` | Exact agent result count. |
| `error_type` | String | `Validation` | Validation or repository message when present. |

## Frequency
Every Find tap.

## User Meaning
User searched for agents in a pincode.

## Analytics Value
Shows location demand and coverage gaps.

## Caveats
Exact pincode is logged by design.

---

# Event: agent_contacted

## Purpose
Records successful launch of an agent contact action.

## Trigger
User taps Call, WhatsApp, or Share and Android accepts the intent.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/suggest/SuggestViewModel.kt`
- Class: `SuggestViewModel`
- Function: `logAgentContactSucceeded`
- Line number: 149

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `calculator` | Calculator flow. |
| `agent_id` | String | `agent_125055_1` | Agent identifier. |
| `agent_contact_type` | String | `whatsapp` | Contact action. |
| `agent_pincode` | String | `125055` | Exact agent pincode. |

## Frequency
Every successful contact intent.

## User Meaning
User contacted or attempted to contact an agent.

## Analytics Value
Measures agent lead/action generation.

## Caveats
Intent success does not prove the call/message/share completed.

---

# Event: agent_contact_failed

## Purpose
Records failed agent contact actions.

## Trigger
Agent Call, WhatsApp, or Share intent throws.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/ui/calculator/suggest/SuggestViewModel.kt`
- Class: `SuggestViewModel`
- Function: `logAgentContactFailed`
- Line number: 155

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `flow` | String | `calculator` | Calculator flow. |
| `agent_id` | String | `agent_125055_1` | Agent identifier. |
| `agent_contact_type` | String | `call` | Contact action. |
| `agent_pincode` | String | `125055` | Exact agent pincode. |
| `error_area` | String | `agent_contact` | Failure area. |
| `error_type` | String | `ActivityNotFoundException` | Throwable class. |

## Frequency
Only on failure.

## User Meaning
User tried to contact an agent but Android could not handle it.

## Analytics Value
Shows agent contact reliability.

## Caveats
Crashlytics also records the exception.

---

# Event: rates_sync_completed

## Purpose
Records successful rates sync worker completion.

## Trigger
`RatesSyncWorker.doWork()` receives Updated or Unchanged.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/data/calculator/RatesSyncWorker.kt`
- Class: `RatesSyncWorker`
- Function: `doWork`
- Line number: 19, 29

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `rates_version` | String | `2026-07-01` | Synced rate version. |
| `sync_result` | String | `updated` | `updated` or `unchanged`. |

## Frequency
Each successful worker run.

## User Meaning
Background rate data is current or unchanged.

## Analytics Value
Monitors data freshness.

## Caveats
Background telemetry, not direct user action.

---

# Event: rates_sync_failed

## Purpose
Records failed rates sync worker runs.

## Trigger
`RatesSyncWorker.doWork()` receives Failed.

## Source
- File: `app/src/main/java/roy/ij/postofficesaathi/data/calculator/RatesSyncWorker.kt`
- Class: `RatesSyncWorker`
- Function: `doWork`
- Line number: 39

## Parameters
| Parameter | Type | Example | Description |
|---|---:|---|---|
| `sync_result` | String | `failed` | Sync failure state. |
| `error_type` | String | `UnknownHostException` | Throwable class. |

## Frequency
Only on worker sync failure.

## User Meaning
Background rate refresh failed.

## Analytics Value
Monitors remote rate data reliability.

## Caveats
Worker may retry before final failure.

---

## Summary Table

| Event | Trigger | Parameters | Purpose | Business Question Answered |
|---|---|---|---|---|
| `screen_viewed` | Screen enters composition | `screen` | Screen reach | Which screens do users reach? |
| `screen_time` | Screen leaves composition | `screen`, `duration_bucket` | Time spent | Where do users spend time? |
| `button_tapped` | Generic controls | `button_id`, `screen` | Low-level interaction | Which controls are used? |
| `storage_permission_result` | Legacy storage permission returns | `flow`, `granted` | Permission outcome | Are permissions blocking saves? |
| `onboarding_page_viewed` | Onboarding page changes | `page_index`, `page_title` | Page reach | Which onboarding pages are seen? |
| `onboarding_completed` | Get Started | `page_index`, `page_title` | Completion | Do users finish onboarding? |
| `onboarding_skipped` | Skip | `page_index`, `page_title` | Skip | Where do users skip onboarding? |
| `theme_changed` | Theme selected | `theme_mode` | Preference | Which theme is preferred? |
| `feedback_email_tapped` | Feedback tapped | none | Feedback intent | How often do users seek support? |
| `rate_app_tapped` | Rate tapped | none | Rating intent | How often do users rate? |
| `review_prompt_requested` | Review policy fires | none | Review prompt request | When are prompts requested? |
| `recent_work_opened` | Recent work open succeeds | `item_type`, `document_name` | Reuse | Is Recent Work useful? |
| `recent_work_open_failed` | Recent work open fails | item/error params | Failure | Are saved files hard to open? |
| `recent_work_shared` | Recent work share succeeds | `item_type`, `document_name` | Reuse/share | Are saved files shared? |
| `recent_work_share_failed` | Recent work share fails | item/error params | Failure | Are saved files hard to share? |
| `forms_index_loaded` | Forms load completes | `from_cache`, `result_count` | Catalog load | Is forms catalog available? |
| `form_search` | Search query changes | `search_text`, `result_count` | Search | What do users search for? |
| `form_search_empty` | Search has no results | `search_text`, `result_count` | Failed search | Which searches fail? |
| `form_download_started` | Open/share form starts | form/action/query params | Demand | Which forms are requested? |
| `form_download_succeeded` | Form prepared | form/document params | Success | Which forms download/cache? |
| `form_download_failed` | Form preparation fails | form/error params | Failure | Which forms fail? |
| `form_opened` | Form open intent succeeds | form/document params | Open | Which forms are opened? |
| `form_open_failed` | Form open intent fails | form/error params | Failure | Are PDFs openable? |
| `form_shared` | Form share intent succeeds | form/document params | Share | Which forms are shared? |
| `form_share_failed` | Form share intent fails | form/error params | Failure | Are PDFs shareable? |
| `pdf_flow_started` | Home Create PDF | `flow` | Flow entry | How many users start PDF creation? |
| `pdf_layout_selected` | Layout chosen | `layout_type` | Layout choice | Which layout is preferred? |
| `camera_permission_result` | Camera permission returns | `layout_type`, `granted` | Permission | Is camera permission blocking capture? |
| `capture_started` | Camera shutter | `layout_type`, `capture_source` | Capture start | Do users use camera? |
| `capture_succeeded` | Last image accepted | `layout_type`, `image_count` | Capture complete | Do users finish capture? |
| `capture_failed` | Camera capture error | capture/error params | Failure | Is camera capture reliable? |
| `capture_prepare_failed` | EXIF/prep failure | layout/error params | Failure | Is image prep reliable? |
| `gallery_import_succeeded` | Gallery import succeeds | `layout_type`, `capture_source` | Import | Do users use gallery? |
| `gallery_import_failed` | Gallery import fails | layout/error params | Failure | Is gallery import reliable? |
| `corner_detection_result` | Corner detection completes | `layout_type`, `used_fallback` | Detection | How often does detection fall back? |
| `corner_detection_failed` | Detection throws | layout/error params | Failure | Is detection stable? |
| `image_adjusted` | Corrected image created | `layout_type` | Correction | Do users apply correction? |
| `image_adjust_failed` | Correction fails | layout/error params | Failure | Is image correction reliable? |
| `image_rotate_failed` | Rotation fails | layout/error params | Failure | Is rotation reliable? |
| `pdf_preview_reset` | Preview reset confirmed | `layout_type` | Reset | Are users undoing layout work? |
| `pdf_create_started` | Save PDF tapped | exact name/layout/image params | Save start | What filenames/names are attempted? |
| `pdf_create_succeeded` | PDF saved | exact name/final filename params | Save success | Which PDF saves succeed? |
| `pdf_create_failed` | PDF save fails | exact name/error params | Failure | Why do PDF saves fail? |
| `pdf_opened` | PDF open succeeds | `pdf_filename` | Open | Do users inspect created PDFs? |
| `pdf_open_failed` | PDF open fails | filename/error params | Failure | Are PDF viewers missing? |
| `pdf_shared` | PDF share succeeds | `pdf_filename` | Share | Are created PDFs shared? |
| `pdf_share_failed` | PDF share fails | filename/error params | Failure | Are PDF shares failing? |
| `calculator_opened` | Calculator home/scheme opens | entry/scheme/rate params | Calculator reach | Which calculators are used? |
| `calculator_input_changed` | Discrete calculator selector changes | field/value/scheme params | Assumption changes | Which controls matter? |
| `calculation_succeeded` | Calculate succeeds | exact input/result params | Result | Which calculations are performed? |
| `calculation_failed` | Calculate fails | exact input/error params | Failure | Where is calculator friction? |
| `calculator_rate_fallback_used` | Fallback rate used | rate/date params | Rate gap | Which rate history is missing? |
| `result_shared` | Calculation share succeeds | share/result params | Share | Which results are shared? |
| `result_share_failed` | Calculation share fails | share/error params | Failure | Is result sharing reliable? |
| `plan_suggested` | Suggest succeeds | amount/top/result params | Recommendations | Which plan suggestions win? |
| `plan_suggest_failed` | Suggest fails | amount/error params | Failure | Why does suggestion fail? |
| `plan_detail_opened` | Suggestion detail opened | scheme/amount/maturity params | Conversion | Which recommendations convert? |
| `agent_search_performed` | Agent Find tapped | `pincode`, `result_count`, `error_type` | Agent search | Which pincodes need agents? |
| `agent_contacted` | Agent contact intent succeeds | agent/contact/pincode params | Lead | Which agents get contact actions? |
| `agent_contact_failed` | Agent contact fails | agent/error params | Failure | Are contact actions reliable? |
| `rates_sync_completed` | Worker sync succeeds | `rates_version`, `sync_result` | Data freshness | Are rates current? |
| `rates_sync_failed` | Worker sync fails | `sync_result`, `error_type` | Failure | Is rate sync reliable? |

## Final Findings

1. Duplicate events fixed:
`pdf_layout_selected`, image rotation button duplicates, `form_download_failed` plus hidden `form_download_failed`, `pdf_create_failed` plus hidden `pdf_create_failed`, `capture_failed` plus hidden `capture_failed`, and redundant settings/help/privacy open events.

2. Redundant events removed:
`settings_opened`, `help_opened`, `privacy_opened`, `scheme_selected`, `td_tenure_changed`, `calculation_performed`, `rd_rebate_opened`, `pmi_opened`, and generated hidden `*_failed` events from `recordError(...)`.

3. Events renamed/replaced:
`calculation_performed` became `calculation_succeeded` and `calculation_failed`; `td_tenure_changed` became `calculator_input_changed`; `scheme_selected` behavior is covered by scheme-level `calculator_opened`.

4. Missing events added:
Forms external failures, PDF processing/open/share failures, storage permission, onboarding page views, recent work open/share success/failure, exact plan suggestion failures, exact Agent Finder contact failures, and calculator result share failures.

5. Parameters added to improve analytics:
Exact `search_text`, exact `customer_name`, exact `requested_pdf_filename`, exact final `pdf_filename`, exact calculator inputs/results, exact `pincode`, exact `agent_pincode`, `entry_point`, `action_type`, `share_channel`, `result_count`, `sync_result`, and `document_name`.
